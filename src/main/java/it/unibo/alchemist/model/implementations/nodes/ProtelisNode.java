/*
 * Copyright (C) 2010-2015, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.nodes;

import it.unibo.alchemist.model.ProtelisIncarnation;
import it.unibo.alchemist.model.implementations.actions.RunProtelisProgram;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.protelis.AlchemistNetworkManager;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.protelis.lang.datatype.DeviceUID;
import org.protelis.vm.ExecutionEnvironment;

/**
 */
public class ProtelisNode extends GenericNode<Object>implements DeviceUID, ExecutionEnvironment {

    private static final long serialVersionUID = 7411790948884770553L;
    private final Map<RunProtelisProgram, AlchemistNetworkManager> netmgrs = new ConcurrentHashMap<>();

    /**
     * Builds a new {@link ProtelisNode}.
     */
    public ProtelisNode() {
        super(true);
    }

    @Override
    protected Object createT() {
        return null;
    }

    @Override
    public String toString() {
        return Long.toString(getId());
    }

    /**
     * Adds a new {@link NetworkManager}.
     * 
     * @param program
     *            the {@link RunProtelisProgram}
     * @param netmgr
     *            the {@link AlchemistNetworkManager}
     */
    public void addNetworkManger(final RunProtelisProgram program, final AlchemistNetworkManager netmgr) {
        netmgrs.put(program, netmgr);
    }

    /**
     * @param program
     *            the {@link RunProtelisProgram}
     * @return the {@link AlchemistNetworkManager} for this specific
     *         {@link RunProtelisProgram}
     */
    public AlchemistNetworkManager getNetworkManager(final RunProtelisProgram program) {
        Objects.requireNonNull(program);
        return netmgrs.get(program);
    }

    private static Molecule makeMol(final String id) {
        return ProtelisIncarnation.instance().createMolecule(id);
    }

    @Override
    public boolean has(final String id) {
        return contains(makeMol(id));
    }

    @Override
    public Object get(final String id) {
        return getConcentration(makeMol(id));
    }

    @Override
    public Object get(final String id, final Object defaultValue) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean put(final String id, final Object v) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Object remove(final String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void commit() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setup() {
        // TODO Auto-generated method stub

    }

}
