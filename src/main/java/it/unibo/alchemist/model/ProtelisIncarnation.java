/*
 * Copyright (C) 2010-2015, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.danilopianini.lang.util.FasterString;
import org.protelis.lang.ProtelisLoader;
import org.protelis.vm.ExecutionContext;
import org.protelis.vm.IProgram;
import org.protelis.vm.impl.DummyContext;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import it.unibo.alchemist.model.implementations.molecules.Molecule;
import it.unibo.alchemist.model.interfaces.IMolecule;
import it.unibo.alchemist.model.interfaces.INode;
import it.unibo.alchemist.model.interfaces.Incarnation;

/**
 * @author Danilo Pianini
 *
 */
public class ProtelisIncarnation implements Incarnation {

	private static final String[] ANS_NAMES = {"ans", "res", "result", "answer", "val", "value"};
	private static final Set<FasterString> NAMES;
	
	private final Cache<String, Optional<IProgram>> cache = CacheBuilder.newBuilder()
		.maximumSize(100)
		.expireAfterAccess(1, TimeUnit.HOURS)
		.expireAfterWrite(1, TimeUnit.HOURS)
		.build();

	static {
		NAMES = Collections.unmodifiableSet(Arrays.stream(ANS_NAMES)
				.flatMap(n -> Arrays.stream(new String[]{n.toLowerCase(Locale.US), n.toUpperCase(Locale.US)}))
				.map(FasterString::new)
				.collect(Collectors.toSet()));
	}
	

	@Override
	public double getProperty(final INode<?> node, final IMolecule mol, final String prop) {
		Object val = node.getConcentration(mol);
		Optional<IProgram> prog = cache.getIfPresent(prop);
		if (prog == null) {
			try {
				prog = Optional.of(ProtelisLoader.parse(prop));
				cache.put(prop, prog);
			} catch (final RuntimeException e) {
				/*
				 * all fine, there is no program to evaluate.
				 */
				prog = Optional.empty();
				cache.put(prop, prog);
			}
		}
		val = preprocess(prog, val);
		if (val instanceof Number) {
			return ((Number) val).doubleValue();
		} else if (val instanceof String) {
			if (val.equals(prop)) {
				return 1;
			}
			return 0;
		} else if (val instanceof Boolean) {
			final Boolean cond = (Boolean) val;
			if (cond) {
				return 1d;
			} else {
				return 0d;
			}
		}
		return Double.NaN;
	}

	private static Object preprocess(final Optional<IProgram> prog, final Object val) {
		try {
			if (prog.isPresent()) {
				final ExecutionContext ctx = new DummyContext();
				final IProgram program = prog.get();
				ctx.setup();
				NAMES.stream().forEach(n -> ctx.putEnvironmentVariable(n.toString(), val));
				program.compute(ctx);
				ctx.commit();
				return program.getCurrentValue();
			}
		} catch (final RuntimeException | Error e) {
			/*
			 * Something went wrong, fallback.
			 */
			return val;
		}
		return val;
	}
	
	@Override
	public IMolecule createMolecule(final String s) {
		return new Molecule(s);
	}

}
