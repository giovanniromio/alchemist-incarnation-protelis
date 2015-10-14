/**
 * 
 */
package it.unibo.alchemist.protelis;

import it.unibo.alchemist.external.cern.jet.random.engine.RandomEngine;
import it.unibo.alchemist.model.implementations.molecules.Molecule;
import it.unibo.alchemist.model.implementations.nodes.ProtelisNode;
import it.unibo.alchemist.model.implementations.positions.LatLongPosition;
import it.unibo.alchemist.model.interfaces.IEnvironment;
import it.unibo.alchemist.model.interfaces.IMapEnvironment;
import it.unibo.alchemist.model.interfaces.IMolecule;
import it.unibo.alchemist.model.interfaces.INode;
import it.unibo.alchemist.model.interfaces.IPosition;
import it.unibo.alchemist.model.interfaces.IReaction;
import it.unibo.alchemist.model.interfaces.ITime;

import org.danilopianini.lang.HashUtils;
import org.danilopianini.lang.PrimitiveUtils;
import org.danilopianini.lang.util.FasterString;
import org.protelis.lang.datatype.DeviceUID;
import org.protelis.lang.datatype.Tuple;
import org.protelis.vm.impl.AbstractExecutionContext;

import java.util.Map;

import com.google.common.collect.MapMaker;

/**
 * @author Danilo Pianini
 *
 */
public class AlchemistExecutionContext extends AbstractExecutionContext {
	
	private static final MapMaker MAPMAKER = new MapMaker();
	private final ProtelisNode node;
	private final IEnvironment<Object> env;
	private final IReaction<Object> react;
	private final RandomEngine rand;
	private ITime previous;
	private int hash;
	
	/**
	 * @param environment the simulation {@link IEnvironment}
	 * @param localNode the local {@link ProtelisNode}
	 * @param reaction the {@link IReaction} hosting the program
	 * @param random the {@link RandomEngine} for this simulation
	 * @param netmgr the {@link AlchemistNetworkManager} to be used
	 */
	public AlchemistExecutionContext(
			final IEnvironment<Object> environment,
			final ProtelisNode localNode,
			final IReaction<Object> reaction,
			final RandomEngine random,
			final AlchemistNetworkManager netmgr) {
		super(netmgr);
		env = environment;
		node = localNode;
		react = reaction;
		rand = random;
	}
	
	@Override
	public DeviceUID getDeviceUID() {
		return node;
	}

	@Override
	public Number getCurrentTime() {
		return react.getTau().toDouble();
	}

	@Override
	public double distanceTo(final DeviceUID target) {
		assert target instanceof ProtelisNode;
		return env.getDistanceBetweenNodes(node, (ProtelisNode) target);
	}

	@Override
	public IPosition getDevicePosition() {
		return env.getPosition(node);
	}

	@Override
	public double nextRandomDouble() {
		return rand.nextDouble();
	}

	@Override
	protected Map<FasterString, Object> currentEnvironment() {
		final Map<FasterString, Object> freshEnv = MAPMAKER.makeMap();
		node.getContents().entrySet().stream().parallel().forEach(e -> {
			final IMolecule key = e.getKey();
			if (key instanceof Molecule) {
				freshEnv.put(((Molecule) key).toFasterString(), e.getValue());
			}
		});
		return freshEnv;
	}

	@Override
	protected void setEnvironment(final Map<FasterString, Object> newEnvironment) {
		newEnvironment.entrySet().forEach(e -> {
			node.setConcentration(new Molecule(e.getKey()), e.getValue());
		});
		if (newEnvironment.size() != node.getChemicalSpecies()) {
			/*
			 * There has been a removal
			 */
			node.getContents().keySet().stream().parallel()
				.filter(mol -> mol instanceof Molecule)
				.filter(mol -> !newEnvironment.containsKey(((Molecule) mol).toFasterString()))
				.forEach(mol -> node.removeConcentration(mol));
		}
	}

	@Override
	protected AbstractExecutionContext instance() {
		return new AlchemistExecutionContext(env, node, react, rand, (AlchemistNetworkManager) getNetworkManager());
	}

	/**
	 * Computes the distance along a map. Requires a {@link IMapEnvironment}.
	 * 
	 * @param dest the destination, as a {@link Tuple} of two values: [latitude, longitude]
	 * @return the distance on a map
	 */
	public double routingDistance(final Tuple dest) {
		if (dest.size() == 2) {
			return routingDistance(new LatLongPosition((Number) dest.get(0), (Number) dest.get(1)));
		}
		throw new IllegalArgumentException(dest + " is not a coordinate I can understand.");
	}
	
	/**
	 * Computes the distance along a map. Requires a {@link IMapEnvironment}.
	 * 
	 * @param dest the destination, in form of {@link ProtelisNode} ID. Non integer numbers will be cast to integers by {@link Number#intValue()}.
	 * @return the distance on a map
	 */
	public double routingDistance(final Number dest) {
		return routingDistance(env.getNodeByID(dest.intValue()));
	}
	
	/**
	 * Computes the distance along a map. Requires a {@link IMapEnvironment}.
	 * 
	 * @param dest the destination, in form of a destination node
	 * @return the distance on a map
	 */
	public double routingDistance(final INode<Object> dest) {
		return routingDistance(env.getPosition(dest));
	}
	
	/**
	 * Computes the distance along a map. Requires a {@link IMapEnvironment}.
	 * 
	 * @param dest the destination
	 * @return the distance on a map
	 */
	public double routingDistance(final IPosition dest) {
		if (env instanceof IMapEnvironment<?>) {
			return ((IMapEnvironment<Object>) env).computeRoute(node, dest).getDistance();
		}
		return getDevicePosition().getDistanceTo(dest);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof AlchemistExecutionContext) {
			final AlchemistExecutionContext ctx = (AlchemistExecutionContext) obj;
			return node.equals(ctx.node) && env.equals(ctx.env) && react.equals(ctx.react) && rand.equals(ctx.rand);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		if (hash == 0) {
			hash = HashUtils.hash32(node, env, react);
		}
		return hash;
	}
	
//	@Override
//	public Number getDeltaTime() {
//		if (previous == null) {
//			previous = react.getTau();
//		}
//		return 1;
//	}
	
}
