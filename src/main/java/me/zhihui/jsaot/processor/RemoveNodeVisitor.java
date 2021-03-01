package me.zhihui.jsaot.processor;

import org.antlr.v4.runtime.tree.ParseTreeProperty;

import me.zhihui.jsaot.parser.JavaScriptParserBaseVisitor;
import me.zhihui.jsaot.symbol.GlobalScope;
import me.zhihui.jsaot.symbol.Scope;

public class RemoveNodeVisitor extends JavaScriptParserBaseVisitor<Object> {
	ParseTreeProperty<Scope> scopes;
	GlobalScope globals;

	public RemoveNodeVisitor(ParseTreeProperty<Scope> scopes,
			GlobalScope globals) {
		this.scopes = scopes;
		this.globals = globals;
	}

}
