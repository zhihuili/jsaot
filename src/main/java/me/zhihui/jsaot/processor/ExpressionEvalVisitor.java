package me.zhihui.jsaot.processor;

import java.util.Stack;

import me.zhihui.jsaot.parser.JavaScriptLexer;
import me.zhihui.jsaot.parser.JavaScriptParser;
import me.zhihui.jsaot.parser.JavaScriptParser.AssignmentOperatorExpressionContext;
import me.zhihui.jsaot.parser.JavaScriptParser.MultiplicativeExpressionContext;
import me.zhihui.jsaot.parser.JavaScriptParser.PostDecreaseExpressionContext;
import me.zhihui.jsaot.parser.JavaScriptParser.RelationalExpressionContext;
import me.zhihui.jsaot.parser.JavaScriptParser.WhileStatementContext;
import me.zhihui.jsaot.parser.JavaScriptParserBaseVisitor;
import me.zhihui.jsaot.processor.expression.EvalResult;
import me.zhihui.jsaot.processor.expression.Operator;
import me.zhihui.jsaot.processor.expression.ReturnValue;
import me.zhihui.jsaot.symbol.FunctionSpace;
import me.zhihui.jsaot.symbol.FunctionSymbol;
import me.zhihui.jsaot.symbol.GlobalScope;
import me.zhihui.jsaot.symbol.MemorySpace;
import me.zhihui.jsaot.symbol.Scope;
import me.zhihui.jsaot.symbol.Symbol;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpressionEvalVisitor extends
		JavaScriptParserBaseVisitor<EvalResult> {
	Logger log = LoggerFactory.getLogger(ExpressionEvalVisitor.class);

	ParseTreeProperty<Scope> scopes;
	GlobalScope globals;
	Scope currentScope;
	MemorySpace globalSpace;
	MemorySpace currentSpace;
	Stack<FunctionSpace> stack = new Stack<FunctionSpace>();
	public static final ReturnValue sharedReturnValue = new ReturnValue();

	public ExpressionEvalVisitor(ParseTreeProperty<Scope> scopes,
			GlobalScope globals, MemorySpace globalSpace) {
		this.scopes = scopes;
		this.globals = globals;
		currentScope = globals;
		this.globalSpace = globalSpace;
		currentSpace = globalSpace;
	}

	@Override
	public EvalResult visitVariableDeclaration(
			JavaScriptParser.VariableDeclarationContext ctx) {
		return evalExpression(ctx);
	}

	private EvalResult evalExpression(ParserRuleContext ctx) {
		String variableName = getIdentifier(ctx);
		if (variableName != null) {
			EvalResult r = visit(ctx.getChild(2));// ctx.getChild(1) is "="
			storeMemory(variableName, r.copy());
		}
		return null;
	}

	private void storeMemory(String id, EvalResult r) {
		currentSpace.put(id, r); // store
		log.debug(currentSpace.toString());
	}

	/**
	 * get left branch identifier
	 */
	private String getIdentifier(ParserRuleContext ctx) {
		ParseTree child0 = ctx.getChild(0);
		if (child0 instanceof JavaScriptParser.AssignableContext
				|| child0 instanceof JavaScriptParser.SingleExpressionContext) {
			ParseTree child0_0 = child0.getChild(0);
			if (child0_0 instanceof JavaScriptParser.IdentifierContext) {
				return child0_0.getChild(0).getText();
			}
		}
		return null;
	}

	/**
	 * call function
	 * 
	 * singleExpression arguments # ArgumentsExpression
	 */
	@Override
	public EvalResult visitArgumentsExpression(
			JavaScriptParser.ArgumentsExpressionContext ctx) {
		String id = getIdentifier(ctx);
		FunctionSymbol fs = (FunctionSymbol) resolveScope(ctx).resolve(id);
		JavaScriptParser.FunctionDeclarationContext functionRoot = fs.getAst();
		JavaScriptParser.FunctionBodyContext body = functionRoot.getChild(
				JavaScriptParser.FunctionBodyContext.class, 0);

		FunctionSpace fspace = new FunctionSpace(fs);
		MemorySpace saveSpace = currentSpace;
		currentSpace = fspace;

		JavaScriptParser.ArgumentsContext args = ctx.getChild(
				JavaScriptParser.ArgumentsContext.class, 0);
		int argsCount = args.getChildCount() - 2;// - "(" ")"
		int i = 0;
		for (Symbol parameter : fs.getMembers().values()) {
			if (i < argsCount) {
				ParseTree argNode = args.getChild(i + 1);
				EvalResult value = visit(argNode);
				fspace.put(parameter.getName(), value.copy());
				i++;
			}
		}

		stack.push(fspace);
		log.debug("push:" + fspace.toString());
		EvalResult result = null;
		try {
			visit(body);
		} catch (ReturnValue rv) {
			result = rv.value;
		}
		log.debug("pop:" + fspace.toString());

		stack.pop();
		currentSpace = saveSpace;
		return result;
	}

	private Scope resolveScope(ParseTree ctx) {
		if (scopes.get(ctx) != null)
			return scopes.get(ctx);
		return resolveScope(ctx.getParent());

	}

	/**
	 * return by throw error
	 */
	@Override
	public EvalResult visitReturnStatement(
			JavaScriptParser.ReturnStatementContext ctx) {
		sharedReturnValue.value = visit(ctx.getChild(1));
		throw sharedReturnValue;
	}

	/**
	 * If '(' expressionSequence ')' statement (Else statement)?
	 */
	@Override
	public EvalResult visitIfStatement(JavaScriptParser.IfStatementContext ctx) {
		processCondition(ctx);
		// TODO else
		return null;
	}

	private EvalResult processCondition(ParserRuleContext node) {
		boolean cond = condition(node);
		EvalResult er;
		if (cond) {
			JavaScriptParser.StatementContext statNode = node.getChild(
					JavaScriptParser.StatementContext.class, 0);
			er = visit(statNode);
			return er == null ? new EvalResult() : er;
		}
		return null;// cond is false
	}

	private boolean condition(ParserRuleContext node) {
		JavaScriptParser.ExpressionSequenceContext boolExpNode = node.getChild(
				JavaScriptParser.ExpressionSequenceContext.class, 0);
		return (Boolean) visit(boolExpNode).getValue();
	}

/**
 * singleExpression ('<' | '>' | '<=' | '>=') singleExpression 
 */
	@Override
	public EvalResult visitRelationalExpression(RelationalExpressionContext ctx) {
		EvalResult a = visit(ctx.getChild(0));
		EvalResult b = visit(ctx.getChild(2));
		String op = ctx.getChild(1).getText();
		return Operator.relational(op, a, b);
	}

	/**
	 * singleExpression ('+' | '-') singleExpression
	 */
	@Override
	public EvalResult visitAdditiveExpression(
			JavaScriptParser.AdditiveExpressionContext ctx) {

		EvalResult a = visit(ctx.getChild(0));
		EvalResult b = visit(ctx.getChild(2));
		EvalResult r = null;
		if ("+".equals(ctx.getChild(1).getText())) {
			r = Operator.add(a, b);

		} else {
			r = Operator.minus(a, b);

		}
		log.debug("Additive:" + r);
		return r;

	}

	/**
	 * singleExpression ('*' | '/' | '%') singleExpression
	 */
	public EvalResult visitMultiplicativeExpression(
			MultiplicativeExpressionContext ctx) {

		EvalResult a = visit(ctx.getChild(0));
		EvalResult b = visit(ctx.getChild(2));
		String op = ctx.getChild(1).getText();
		EvalResult r = null;
		if ("*".equals(op)) {
			r = Operator.multi(a, b);

		} else if ("/".equals(op)) {
			r = Operator.divide(a, b);

		}
		if ("%".equals(op)) {
			r = Operator.mod(a, b);

		}
		log.debug("Multi:" + r);
		return r;

	}

	@Override
	public EvalResult visitLiteral(JavaScriptParser.LiteralContext ctx) {
		EvalResult r = new EvalResult();

		ParseTree tree = ctx.getChild(0);
		if (tree instanceof TerminalNode) {
			TerminalNode leaf = (TerminalNode) tree;
			if (leaf.getSymbol().getType() == JavaScriptLexer.StringLiteral) {
				r.setType(EvalResult.EvalResultType.STRING);
				r.setValue(leaf.getText());
			}
		}
		if (tree instanceof ParserRuleContext) {
			if (tree instanceof JavaScriptParser.NumericLiteralContext) {
				String numberStr = tree.getChild(0).getText();
				if (numberStr.contains(".")) {
					r.setType(EvalResult.EvalResultType.FLOAT);
					r.setValue(Float.valueOf(numberStr));
				} else {
					r.setType(EvalResult.EvalResultType.LONG);
					r.setValue(Long.valueOf(numberStr));
				}
			}
		}
		log.debug("Literal:" + r);
		return r;
	}

	@Override
	public EvalResult visitFunctionDeclaration(
			JavaScriptParser.FunctionDeclarationContext ctx) {
		return null;
	}

	@Override
	public EvalResult visitIdentifier(JavaScriptParser.IdentifierContext ctx) {
		String id = ctx.getChild(0).getText();
		MemorySpace space = getSpaceWithSymbol(id);
		if (space == null)
			space = currentSpace;
		Object o = space.get(id);
		if (o != null)
			return (EvalResult) o;
		return null;
	}

	/**
	 * singleExpression {this.notLineTerminator()}? '--'
	 */
	@Override
	public EvalResult visitPostDecreaseExpression(
			PostDecreaseExpressionContext ctx) {
		String id = getIdentifier(ctx);
		if (id == null) {
			return null;
		}
		EvalResult value = visit(ctx.getChild(0));// call visitIdentifier
		value.decrease();
		log.debug(currentSpace.toString());

		return value;
	}

	/**
	 * <assoc=right> singleExpression assignmentOperator singleExpression
	 */
	@Override
	public EvalResult visitAssignmentOperatorExpression(
			AssignmentOperatorExpressionContext ctx) {
		String id = getIdentifier(ctx);
		if (id == null)
			return null;
		String op = ctx.getChild(1).getText();
		switch (op) {
		case "*=":
			currentSpace.put(
					id,
					Operator.multi(visit(ctx.getChild(0)),
							visit(ctx.getChild(2))));
		case "+=":
			currentSpace.put(id, Operator.add(visit(ctx.getChild(0)),
					visit(ctx.getChild(2))));
		}
		return null;
	}

	/**
	 * While '(' expressionSequence ')' statement
	 */
	@Override
	public EvalResult visitWhileStatement(WhileStatementContext ctx) {
		if (processCondition(ctx) != null)
			visitWhileStatement(ctx);
		return null;
	}

	@Override
	public EvalResult visitAssignmentExpression(
			JavaScriptParser.AssignmentExpressionContext ctx) {

		return evalExpression(ctx);
	}

	private MemorySpace getSpaceWithSymbol(String id) {
		if (stack.size() > 0 && stack.peek().get(id) != null) { // in top stack?
			return stack.peek();
		}
		if (globalSpace.get(id) != null)
			return globalSpace; // in globals?
		return null; // nowhere
	}

}
