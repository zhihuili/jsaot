package me.zhihui.jsaot;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import me.zhihui.jsaot.parser.JavaScriptLexer;
import me.zhihui.jsaot.parser.JavaScriptParser;
import me.zhihui.jsaot.parser.JavaScriptParser.ProgramContext;
import me.zhihui.jsaot.processor.DeclProcessor;
import me.zhihui.jsaot.processor.ExpressionEvalVisitor;
import me.zhihui.jsaot.symbol.GlobalScope;
import me.zhihui.jsaot.symbol.MemorySpace;
import me.zhihui.jsaot.symbol.Scope;

import org.antlr.v4.gui.Trees;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
	static Logger log = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) throws IOException {
		String inputFile = App.class.getClassLoader()
				.getResource("sample/3.js").getPath();
		if (args.length > 0)
			inputFile = args[0];
		InputStream is = new FileInputStream(inputFile);
		input(is);
	}

	public static void input(InputStream is) throws IOException {
		Lexer lexer = new JavaScriptLexer(CharStreams.fromStream(is));
		TokenStream tokenStream = new CommonTokenStream(lexer);
		JavaScriptParser parser = new JavaScriptParser(tokenStream);
		ProgramContext root = parser.program();

//		 Trees.inspect(root, parser);

		ParseTreeWalker walker = new ParseTreeWalker();
		ParseTreeProperty<Scope> scopes = new ParseTreeProperty<Scope>();
		GlobalScope globals = null;
		MemorySpace globalSpace = new MemorySpace("global");
		DeclProcessor def = new DeclProcessor(scopes, globals);
		walker.walk(def, root);
		printLine();
//
		ExpressionEvalVisitor evalVisitor = new ExpressionEvalVisitor(scopes,
				globals, globalSpace);
		evalVisitor.visit(root);

		log.info("global space: " + globalSpace.toString());
		printLine();
		output(root);
	}

	private static void output(ProgramContext t) {
		String code = t.getText();
		int l = code.length();
		System.out.println(code.substring(0, l - 5));// rm <EOF>
	}

	private static void printLine() {
		log.debug("==============================================================================================");
	}
}
