package me.zhihui.jsaot.symbol;

/***
 * Excerpted from "Language Implementation Patterns",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/tpdsl for more book information.
 ***/
import java.util.Map;
import java.util.LinkedHashMap;

import me.zhihui.jsaot.parser.JavaScriptParser;

public class FunctionSymbol extends ScopedSymbol {
	private Map<String, Symbol> formalArgs = new LinkedHashMap<String, Symbol>();
	private JavaScriptParser.FunctionDeclarationContext functionAst;

	public FunctionSymbol(String name, Scope parent) {
		super(name, parent);
	}

	public void setAst(JavaScriptParser.FunctionDeclarationContext ast) {
		this.functionAst = ast;
	}

	public JavaScriptParser.FunctionDeclarationContext getAst() {
		return functionAst;
	}

	public Map<String, Symbol> getMembers() {
		return formalArgs;
	}

	public String getName() {
		return name + "(" + formalArgs.keySet().toString() + ")";
	}

	public String toString() {
		return getName() + functionAst;
	}
}
