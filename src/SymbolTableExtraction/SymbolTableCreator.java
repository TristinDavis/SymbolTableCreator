package SymbolTableExtraction;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class SymbolTableCreator extends ASTVisitor{
	
	private String filePath;
	private String documentText;
	private HashMap<String, HashMap<String, ArrayList<String>>> typeToVariableMap = new HashMap<>();
	private HashMap<String, HashMap<String, String>> varToTypeMap = new HashMap<>();
	
	private boolean extracted = false;
	
	public SymbolTableCreator(String inputJavaFilePath) throws FileNotFoundException{
		this.filePath = inputJavaFilePath;
		Scanner scanner = new Scanner(new File(inputJavaFilePath));
		String fileString = scanner.nextLine();
		while (scanner.hasNextLine()) {
			fileString = fileString + "\n" + scanner.nextLine();
		}
		this.documentText = fileString;
		scanner.close();
	}
	
		
	public boolean visit(MethodDeclaration node){
		String  methodBody = node.getBody().toString();
		String text = node.toString();
		String methodSignature = text.substring(0, text.indexOf(methodBody));
		HashMap<String, ArrayList<String>> typeToVarInsideMethod = new HashMap<>();
		HashMap<String, String> varToTypeInsideMethod = new HashMap<>();
		
		ArrayList<String> apis = new ArrayList<>();
		ASTVisitor methodInvocationASTVisitor = new ASTVisitor() {
			public boolean visit(VariableDeclarationStatement varDec){
				//System.err.println(varDec.toString());
				String type = varDec.getType().toString();
				for(Object obj : varDec.fragments()){
					VariableDeclarationFragment frag = (VariableDeclarationFragment) obj;
					String t = frag.resolveBinding().getType().getQualifiedName();
					if(t != null){
						type = t;
					}
					ArrayList<String> vars = typeToVarInsideMethod.get(type);
					if(vars == null){
						vars = new ArrayList<String>();
					}
					String varName = frag.getName().getIdentifier();
					
					vars.add(varName);
					typeToVarInsideMethod.put(type, vars);
					varToTypeInsideMethod.put(varName, type);
					//System.out.println(t + " " + varName);
				}
				return true;
			}
		}; 
		
		node.accept(methodInvocationASTVisitor);
		
		this.typeToVariableMap.put(methodSignature, typeToVarInsideMethod);
		this.varToTypeMap.put(methodSignature, varToTypeInsideMethod);
		
		return true;
	}
	
	public void extract() {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		
		Map<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		parser.setCompilerOptions(options);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(this.documentText.toCharArray());
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		String[] sources = { "" };
		String[] classpath = { System.getProperty("java.home") + "/lib/rt.jar" };
		parser.setUnitName(this.filePath);
		parser.setEnvironment(classpath, sources, new String[] { "UTF-8" }, true);
		
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		cu.accept(this);	
		extracted = true;
	}
	
	public HashMap<String, HashMap<String, ArrayList<String>>> getTypeToVariableMap() throws Exception{
		if(extracted == false){
			throw new Exception("First call extract method");
		}
		return this.typeToVariableMap;
	}
	
	
	public HashMap<String, HashMap<String, String>> getVarToTypeMap() throws Exception{
		if(extracted == false){
			throw new Exception("First call extract method");
		}
		return this.varToTypeMap;
	}
}
