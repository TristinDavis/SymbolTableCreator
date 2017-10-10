package SymbolTableExtraction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import util.Util;



public class Main {
	public static void main(String args[]) throws IOException {
		
		try (BufferedReader br = new BufferedReader(new FileReader("tests/file_list.txt"))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	String infile = line;
				if(args.length > 0) 
		           infile = args[0];
				SymbolTableCreator methodExtractor = new SymbolTableCreator(infile);
				methodExtractor.extract();
				try {
					HashMap<String, HashMap<String, ArrayList<String>>> typeToVarMap = methodExtractor.getTypeToVariableMap();
					HashMap<String, HashMap<String, String>> varToTypeMap = methodExtractor.getVarToTypeMap();
					
					Set<String> methodSignatures = typeToVarMap.keySet();
					for(String methodSignature : methodSignatures){
						HashMap<String, ArrayList<String>> typeToVar = typeToVarMap.get(methodSignature);
						HashMap<String, String> varToType = varToTypeMap.get(methodSignature);
						System.out.println(methodSignature);
						System.out.println("Type To Var : {");
						for(String type : typeToVar.keySet()){
							System.out.print(type + " : [");
							ArrayList<String> vars = typeToVar.get(type);
							for(String var : vars){
								System.out.print(var + " , ");
							}
							System.out.println("]");
						}
						System.out.println("}");
						System.out.print("Var To Type : \n{");
						for(String var : varToType.keySet()){
							System.out.print("(" + var + " : " + varToType.get(var) + "), ");
						}
						System.out.println("}\n\n\n");
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				} 
		    }
		} 
	 }
}
