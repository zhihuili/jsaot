package me.zhihui.jsaot.processor;

import java.util.Stack;

import me.zhihui.jsaot.parser.JavaScriptLexer;
import me.zhihui.jsaot.parser.JavaScriptParser;
import me.zhihui.jsaot.parser.JavaScriptParserBaseVisitor;
import me.zhihui.jsaot.processor.expression.EvalResult;
import me.zhihui.jsaot.processor.expression.Operator;
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
			storeMemory(variableName, r);
		}
		return null;
	}

	private void storeMemory(String id, EvalResult r) {
		MemorySpace space = getSpaceWithSymbol(id);
		if (space == null)
			space = currentSpace; // create in current space
		space.put(id, r); // store
	}

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

	@Override
	/**
	 * call function
	 */
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
				fspace.put(parameter.getName(), value);
				i++;
			}
		}

		stack.push(fspace);
		log.debug(fspace.toString());
		EvalResult result = null;
		try {
			// visit(body);
		} catch (ReturnValue rv) {
			result = rv.value;
		}
		stack.pop();
		currentSpace = saveSpace;
		return result;
	}

	private Scope resolveScope(ParseTree ctx) {
		if (scopes.get(ctx) != null)
			return scopes.get(ctx);
		return resolveScope(ctx.getParent());

	}

	@Override
	public EvalResult visitReturnStatement(
			JavaScriptParser.ReturnStatementContext ctx) {
		sharedReturnValue.value = visit(ctx);
		throw sharedReturnValue;
	}

	@Override
	public EvalResult visitIfStatement(JavaScriptParser.IfStatementContext ctx) {
		JavaScriptParser.ExpressionSequenceContext boolExpNode = ctx.getChild(
				JavaScriptParser.ExpressionSequenceContext.class, 0);
		boolean cond = (Boolean) visit(boolExpNode).getValue();
		if (cond) {
			JavaScriptParser.StatementContext statNode = ctx.getChild(
					JavaScriptParser.StatementContext.class, 0);
			visit(statNode);
		}
		return null;
	}

	@Override
	/**
	 * (+|- a b)
	 */
	public EvalResult visitAdditiveExpression(
			JavaScriptParser.AdditiveExpressionContext ctx) {

		EvalResult a = visit(ctx.getChild(0));
		EvalResult b = visit(ctx.getChild(2));
		EvalResult r = null;
		if ("+".equals(ctx.getChild(1).getText())) {
			r = Operator.add(a, b);

		}
		log.debug("Additive:" + r);
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
