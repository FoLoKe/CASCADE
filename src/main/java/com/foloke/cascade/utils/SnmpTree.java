package com.foloke.cascade.utils;

import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SnmpTree {
    private final Node root;

    public SnmpTree() {
        root = new Node();
    }

    public void put(VariableBinding varBinding) {
        final Node[] currentNode = {root}; // suggested final?

        Arrays.stream(varBinding.getOid().getValue()).forEach(oidStepValue ->
                currentNode[0].children.stream().filter((child) -> child.param == oidStepValue).findFirst().ifPresentOrElse(
                node -> currentNode[0] = node,
                () -> {
                    Node newNode = new Node();
                    newNode.parent = currentNode[0];
                    newNode.param = oidStepValue;
                    currentNode[0] = newNode;
                }
        ));

        currentNode[0].data = varBinding.getVariable();
    }

    //public void put ();

    public static class Node {
        private Variable data;
        private int param;
        private Node parent;
        private final List<Node> children = new ArrayList<>();
    }
}
