/**
 * 
 */
package it.unibo.alchemist.protelis;

import it.unibo.alchemist.model.implementations.actions.ProtelisProgram;
import it.unibo.alchemist.model.implementations.nodes.ProtelisNode;
import it.unibo.alchemist.model.interfaces.IEnvironment;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.protelis.lang.datatype.DeviceUID;
import org.protelis.vm.NetworkManager;
import org.protelis.vm.util.CodePath;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Emulates a {@link NetworkManager}. This particular network manager does not
 * send messages istantly. Instead, it records the last message to send, and
 * only when {@link #simulateMessageArrival()} is called the transfer is
 * actually done.
 * 
 * @author Danilo Pianini
 *
 */
public final class AlchemistNetworkManager implements NetworkManager, Serializable {
	
	private static final long serialVersionUID = -7028533174885876642L;
	private final IEnvironment<Object> env;
	private final ProtelisNode node;
	private final ProtelisProgram prog;
	private Map<DeviceUID, Map<CodePath, Object>> msgs = new LinkedHashMap<>();
	private Map<CodePath, Object> toBeSent;

	/**
	 * @param environment
	 *            the environment
	 * @param local
	 *            the node
	 * @param program
	 *            the {@link ProtelisProgram}
	 */
	public AlchemistNetworkManager(final IEnvironment<Object> environment, final ProtelisNode local, final ProtelisProgram program) {
		env = environment;
		node = local;
		prog = program;
	}
	
	@Override
	public Map<DeviceUID, Map<CodePath, Object>> takeMessages() {
		final Map<DeviceUID, Map<CodePath, Object>> res = msgs;
		msgs = new LinkedHashMap<>();
		return res;
	}

	@Override
	public void sendMessage(final Map<CodePath, Object> toSend) {
		toBeSent = toSend;
	}
	
	/**
	 *  
	 */
	@SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
	public void simulateMessageArrival() {
		Objects.requireNonNull(toBeSent);
		if (!toBeSent.isEmpty()) {
			env.getNeighborhood(node).forEach(n -> {
				if (n instanceof ProtelisNode) {
					final AlchemistNetworkManager destination = ((ProtelisNode) n).getNetworkManager(prog);
					if (destination != null) {
						/*
						 * The node is running the program. Otherwise, the
						 * program is discarded
						 */
						destination.msgs.put(node, toBeSent);
					}
				}
			});
		}
		toBeSent = null;
	}
	
}
