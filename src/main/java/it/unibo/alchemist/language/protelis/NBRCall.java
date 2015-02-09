/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.language.protelis;

import gnu.trove.list.TByteList;
import gnu.trove.map.TIntObjectMap;
import it.unibo.alchemist.language.protelis.datatype.Field;
import it.unibo.alchemist.language.protelis.interfaces.AnnotatedTree;
import it.unibo.alchemist.language.protelis.util.CodePath;
import it.unibo.alchemist.language.protelis.util.Stack;
import it.unibo.alchemist.model.interfaces.IEnvironment;
import it.unibo.alchemist.model.interfaces.INode;

import java.util.Map;

/**
 * @author Danilo Pianini
 *
 * Implementation of 'nbr' operator
 */
public class NBRCall extends AbstractAnnotatedTree<Field> {

	private static final long serialVersionUID = 5255917527687990281L;
	private static final byte BRANCH = 1;
	private final IEnvironment<Object> env;

	public NBRCall(final AnnotatedTree<?> body, final IEnvironment<Object> environment) {
		super(body);
		env = environment;
	}
	
	@Override
	public NBRCall copy() {
		final NBRCall res = new NBRCall(deepCopyBranches().get(0), env);
		return res;
	}

	@Override
	public void eval(final INode<Object> sigma, final TIntObjectMap<Map<CodePath, Object>> theta, final Stack gamma, final Map<CodePath, Object> lastExec, final Map<CodePath, Object> newMap, final TByteList currentPosition) {
		final AnnotatedTree<?> branch = getBranch(0);
		currentPosition.add(BRANCH);
		branch.eval(sigma, theta, gamma, lastExec, newMap, currentPosition);
		final CodePath childPath = new CodePath(currentPosition);
		removeLast(currentPosition);
		final CodePath currentPath = new CodePath(currentPosition);
		final Object childVal = branch.getAnnotation();
		newMap.put(childPath, childVal);
		final Field res;
		if(theta == null) {
			final Field tmp = lastExec == null ? null : (Field) lastExec.get(currentPath);
			res = tmp == null? Field.create(1) : tmp;
		} else {
			res = Field.create(theta.size() + 1);
			theta.forEachEntry((n, pathsMap) -> {
				final Object val = pathsMap.get(childPath);
				if(val != null) {
					res.addSample(env.getNodeByID(n), val);
				} else {
					theta.remove(n);
				}
				return true;
			});
		}
		res.addSample(sigma, childVal);
		//newMap.put(currentPath, res);
		setAnnotation(res);
	}

	@Override
	protected String asString() {
		return "nbr ( "+getBranch(0)+" )";
	}
}