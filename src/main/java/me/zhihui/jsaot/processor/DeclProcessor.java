package me.zhihui.jsaot.processor;

import me.zhihui.jsaot.parser.JavaScriptParser;
import me.zhihui.jsaot.parser.JavaScriptParserBaseListener;
import me.zhihui.jsaot.symbol.FunctionSymbol;
import me.zhihui.jsaot.symbol.GlobalScope;
import me.zhihui.jsaot.symbol.LocalScope;
import me.zhihui.jsaot.symbol.Scope;
import me.zhihui.jsaot.symbol.Symbol;
import me.zhihui.jsaot.symbol.VariableSymbol;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeclProcessor extends JavaScriptParserBaseListener {
	Logger log = LoggerFactory.getLogger(DeclProcessor.class);
	ParseTreeProperty<Scope> scopes;
	GlobalScope globals;
	Scope currentScope; // define symbols in this scope

	public DeclProcessor(ParseTreeProperty<Scope> scopes, GlobalScope globals) {
		this.scopes = scopes;
		this.globals = globals;
	}

	public void enterProgram(JavaScriptParser.ProgramContext ctx) {
		globals = new GlobalScope(null);
		currentScope = globals;
		saveScope(ctx, currentScope);
	}

	public void exitProgram(JavaScriptParser.ProgramContext ctx) {
		log.debug(globals.toString());
	}

	@Override
	public void enterFunctionDeclaration(
			JavaScriptParser.FunctionDeclarationContext ctx) {

		String name = ctx.getChild(1).getChild(0).getText();

		FunctionSymbol function = new FunctionSymbol(name, currentScope);
		function.setAst(ctx);
		currentScope.define(function); // Define function in current scope
		saveScope(ctx, function);
		currentScope = function; // Current scope is now function scope
	}

	public void enterAnoymousFunctionDecl(
			JavaScriptParser.AnoymousFunctionDeclContext ctx) {
		FunctionSymbol function = new FunctionSymbol("anoymous function",
				currentScope);
		saveScope(ctx, function);
		currentScope = function;
	}

	public void exitAnoymousFunctionDecl(
			JavaScriptParser.AnoymousFunctionDeclContext ctx) {
		currentScope = currentScope.getEnclosingScope();
	}

	void saveScope(ParserRuleContext ctx, Scope s) {
		scopes.put(ctx, s);
	}

	@Override
	public void exitFunctionDeclaration(
			JavaScriptParser.FunctionDeclarationContext ctx) {
		currentScope = currentScope.getEnclosingScope(); // pop scope
	}

	public void enterBlock(JavaScriptParser.BlockContext ctx) {
		// push new local scope
		currentScope = new LocalScope(currentScope);
		saveScope(ctx, currentScope);
	}

	public void exitBlock(JavaScriptParser.BlockContext ctx) {
		currentScope = currentScope.getEnclosingScope(); // pop scope
	}

	public void exitVariableDeclaration(
			JavaScriptParser.VariableDeclarationContext ctx) {
		String name = ctx.getChild(0).getChild(0).getChild(0).getText();
		log.debug(currentScope.getScopeName() + " has variable: " + name);
		defineVar(name);
	}

	public void exitFormalParameterArg(
			JavaScriptParser.FormalParameterArgContext ctx) {
		String name = ctx.getChild(0).getChild(0).getChild(0).getText();
		log.debug(currentScope.getScopeName() + " has parameter: " + name);
		Symbol s = defineVar(name);
		if (currentScope instanceof FunctionSymbol) {
			((FunctionSymbol) currentScope).getMembers().put(name, s);
		}
	}

	private Symbol defineVar(String name) {
		VariableSymbol var = new VariableSymbol(name, null);
		currentScope.define(var); // Define symbol in current scope
		return var;
	}

	@Override
	public void enterArgumentsExpression(
			JavaScriptParser.ArgumentsExpressionContext ctx) {
		// System.err.println(ctx.toStringTree());
	}
}
