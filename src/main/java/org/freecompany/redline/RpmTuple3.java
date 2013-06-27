package org.freecompany.redline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.freecompany.redline.header.Flags;

public class RpmTuple3 {

	private String name=null;
	private String version="";
	private Integer operator=Flags.INTERP;

	public static RpmTuple3 builder() {
		return new RpmTuple3();
	}

	/**
	 * set name of dependency/conflict item
	 * @param name
	 * @return
	 */
	public RpmTuple3 setName(String name) {
		this.name = name;
		return this;
	}

	/**
	 * put additional details - version and operator for conflicts/dependency item
	 * @param version
	 * @param operator
	 * @return
	 */
	public RpmTuple3 setDetail(String version, Integer operator) {
		this.version = version;
		this.operator = operator;
		return this;
	}

	public static String[] getNameArray(List<RpmTuple3> conflicts) {
		String[] names=new String[conflicts.size()];
		for(int i =0;i<conflicts.size();i++){
			names[i]=conflicts.get(i).getName();
		}
		return names;
	}
	public static String[] getVersionArray(List<RpmTuple3> conflicts) {
		String[] versions=new String[conflicts.size()];
		for(int i =0;i<conflicts.size();i++){
			versions[i]=conflicts.get(i).getVersion();
		}
		return versions;
	}
	public static int[] getOperatorsArray(List<RpmTuple3> conflicts) {
		int[] operators=new int[conflicts.size()];
		for(int i =0;i<conflicts.size();i++){
			if(conflicts.get(i).getOperator()!=null){
				operators[i]=conflicts.get(i).getOperator();
			}
		}
		return operators;
	}
	
	public String getName() {
		return name;
	}
	public Integer getOperator() {
		return operator;
	}
	public String getVersion() {
		return version;
	}

}
