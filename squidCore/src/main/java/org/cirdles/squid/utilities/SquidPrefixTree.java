/*
 * Copyright 2017 CIRDLES.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cirdles.squid.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author James F. Bowring
 */
public class SquidPrefixTree {

    private final static Character ROOT = "/".toCharArray()[0];
    private final static Character LEAF = "\\".toCharArray()[0];

    private SquidPrefixTree parent;
    private List<SquidPrefixTree> children;
    private SquidPrefixTreeNode node;
    private String stringValue;
    private int countOfSpecies;
    private int countOfScans;
    private int countOfDups;
    private int countOfLeaves;
    private Map<Integer, Integer> mapOfSpeciesFrequencies;
    private Map<Integer, Integer> mapOfScansFrequencies;

    public SquidPrefixTree() {
        this(new SquidPrefixTreeNode(ROOT));
    }

    public SquidPrefixTree(SquidPrefixTreeNode node) {
        this.parent = null;
        this.children = new ArrayList<>();
        this.node = node;
        this.stringValue = node.getValue().toString().toUpperCase(Locale.US);
        this.countOfSpecies = 0;
        this.countOfScans = 0;
        this.countOfDups = 0;
        this.countOfLeaves = 1;
        mapOfSpeciesFrequencies = new HashMap<>();
        mapOfScansFrequencies = new HashMap<>();
    }

    public SquidPrefixTree insert(String myWord) {
        SquidPrefixTree target = this;
        String word = myWord.toUpperCase(Locale.US);
        boolean hasDupString = containsDupString(word);
        for (int i = 0; i < word.length(); i++) {
            if (!hasDupString) {

                SquidPrefixTreeNode currentNode = new SquidPrefixTreeNode(word.charAt(i));
                target = target.findTargetChild(currentNode);
                target.stringValue = word.substring(0, i + 1);

                // add special leaf condition if last letter
                if (i == (word.length() - 1)) {
                    createLeafNode(target);
                }
            } else {
                String remainingString = word.substring(i, word.length());
                Pattern pattern = Pattern.compile("-DUP-.*");
                Matcher matcher = pattern.matcher(remainingString);
                SquidPrefixTreeNode currentNode;

                if (matcher.matches()) {
                    currentNode = new SquidPrefixTreeNode(remainingString.substring(0, 5));
                    //this makes i jump to index after -DUP-
                    i = i + 4;
                } else {
                    currentNode = new SquidPrefixTreeNode(word.charAt(i));
                }
                target = target.findTargetChild(currentNode);
                target.stringValue = word.substring(0, i + 1);

                if (i == (word.length() - 1)) {
                    SquidPrefixTree leaf = createLeafNode(target);
                    leaf.countOfDups = 1;
                }
            }
        }

        return target;
    }

    private SquidPrefixTree createLeafNode(SquidPrefixTree target) {
        SquidPrefixTree leaf = new SquidPrefixTree(new SquidPrefixTreeNode(LEAF));
        leaf.parent = target;
        target.children.add(leaf);

        return leaf;
    }

    private boolean containsDupString(String word) {
        return word.contains("-DUP-");
    }

    public SquidPrefixTree findTargetChild(SquidPrefixTreeNode node) {
        SquidPrefixTree tree = null;

        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).getNode().getValue().compareTo(node.getValue()) == 0) {
                tree = children.get(i);
            }
        }

        if (tree == null) {
            tree = new SquidPrefixTree(node);
            parent = this;
            children.add(tree);
        }
        return tree;
    }

    public SquidPrefixTree findPrefix(String prefix) {
        SquidPrefixTree target = this;
        for (int i = 0; i < prefix.length(); i++) {
            Character letter = prefix.charAt(i);
            target = target.findTargetChild(new SquidPrefixTreeNode(letter));
        }

        return target;
    }

    public void prepareStatistics() {
        sort();
        countAnalysisLeaves();
        countSpeciesFrequencies();
        countScansFrequencies();
        countDups();
    }

    private void sort() {
        sortChildren(children);
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).hasChildren()) {
                sortChildren(children.get(i).getChildren());
            }
        }
    }

    private void sortChildren(List<SquidPrefixTree> children) {
        Collections.sort(children, (SquidPrefixTree pt1, SquidPrefixTree pt2)
                -> (pt1.getNode().getValue().compareTo(pt2.getNode().getValue())));
    }

    private int countAnalysisLeaves() {
        int retVal = 0;
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).getNode().getValue().compareTo(String.valueOf(LEAF)) == 0) {
                retVal++;
            } else {
                int childCountOfLeaves = children.get(i).countAnalysisLeaves();
                // sum leaves recursively
                retVal += childCountOfLeaves;
                // set local count of leaves below this node
                children.get(i).setCountOfLeaves(childCountOfLeaves);
                // this will get overwritten except at root
                countOfLeaves = retVal;
            }
        }
        return retVal;
    }

    private Map<Integer, Integer> countSpeciesFrequencies() {

        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).getNode().getValue().compareTo(String.valueOf(LEAF)) == 0) {
                int childCountOfSpecies = children.get(i).countOfSpecies;
                mapOfSpeciesFrequencies.put(childCountOfSpecies, 1);
            } else {
                Map<Integer, Integer> speciesMap = children.get(i).countSpeciesFrequencies();
                updateFrequencyMap(mapOfSpeciesFrequencies, speciesMap);
            }
        }
        return mapOfSpeciesFrequencies;
    }

    private Map<Integer, Integer> countScansFrequencies() {

        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).getNode().getValue().compareTo(String.valueOf(LEAF)) == 0) {
                int childCountOfScans = children.get(i).countOfScans;
                mapOfScansFrequencies.put(childCountOfScans, 1);
            } else {
                Map<Integer, Integer> scansMap = children.get(i).countScansFrequencies();
                updateFrequencyMap(mapOfScansFrequencies, scansMap);
            }
        }
        return mapOfScansFrequencies;
    }

    private int countDups() {
        int retVal = 0;
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).getNode().getValue().compareTo(String.valueOf(LEAF)) == 0) {
                retVal += children.get(i).countOfDups;
            } else {
                int childCountOfDups = children.get(i).countDups();
                // sum leaves recursively
                retVal += childCountOfDups;
                // set local count of leaves below this node
                children.get(i).setCountOfDups(childCountOfDups);
                // this will get overwritten except at root
                countOfDups = retVal;
            }
        }
        return retVal;
    }

    private void updateFrequencyMap(Map<Integer, Integer> targetMap, Map<Integer, Integer> sourceMap) {
        for (Map.Entry<Integer, Integer> entry : sourceMap.entrySet()) {
            int key = entry.getKey();
            int count = entry.getValue();
            if (targetMap.containsKey(key)) {
                targetMap.put(key, targetMap.get(key) + count);
            } else {
                targetMap.put(key, count);
            }
        }
    }

    public String buildSummaryDataString() {
        // build species and scans count string        
        StringBuilder speciesCountBuffer = new StringBuilder();
        for (Integer count : mapOfSpeciesFrequencies.keySet()) {
            speciesCountBuffer.append("[" + String.format("%1$ 2d", count) + " in " + String.format("%1$ 3d", mapOfSpeciesFrequencies.get(count)) + "]");
        }
        String speciesCounts = speciesCountBuffer.toString();

        StringBuilder scansCountsBuffer = new StringBuilder();
        for (Integer count : mapOfScansFrequencies.keySet()) {
            scansCountsBuffer.append("[" + String.format("%1$ 2d", count) + " in " + String.format("%1$ 3d", mapOfScansFrequencies.get(count)) + "]");
        }
        String scansCounts = scansCountsBuffer.toString();

        String summary = " Analyses=" + String.format("%1$ 3d", countOfLeaves)
                + "; Dups=" + String.format("%1$ 3d", countOfDups)
                + "; Species:" + speciesCounts
                + "; Scans:" + scansCounts;

        return summary;
    }

    public boolean hasChildren() {
        return children.size() > 0;
    }

    public boolean isleaf() {
        return getNode().getValue().compareTo(String.valueOf(LEAF)) == 0;
    }

    /**
     * @return the parent
     */
    public SquidPrefixTree getParent() {
        return parent;
    }

    /**
     * @return the children
     */
    public List<SquidPrefixTree> getChildren() {
        return children;
    }

    /**
     * @return the charValue
     */
    public SquidPrefixTreeNode getNode() {
        return node;
    }

    /**
     * @return the stringValue
     */
    public String getStringValue() {
        return stringValue;
    }

    /**
     * @return the countOfSpecies
     */
    public int getCountOfSpecies() {
        return countOfSpecies;
    }

    /**
     * @param countOfSpecies the countOfSpecies to set
     */
    public void setCountOfSpecies(int countOfSpecies) {
        this.countOfSpecies = countOfSpecies;
    }

    /**
     * @return the countOfScans
     */
    public int getCountOfScans() {
        return countOfScans;
    }

    /**
     * @param countOfScans the countOfScans to set
     */
    public void setCountOfScans(int countOfScans) {
        this.countOfScans = countOfScans;
    }

    /**
     * @return the countOfDups
     */
    public int getCountOfDups() {
        return countOfDups;
    }

    /**
     * @param countOfDups the countOfDups to set
     */
    public void setCountOfDups(int countOfDups) {
        this.countOfDups = countOfDups;
    }

    /**
     * @return the countOfLeaves
     */
    public int getCountOfLeaves() {
        return countOfLeaves;
    }

    /**
     * @param countOfLeaves the countOfLeaves to set
     */
    public void setCountOfLeaves(int countOfLeaves) {
        this.countOfLeaves = countOfLeaves;
    }

    /**
     * @return the mapOfSpeciesFrequencies
     */
    public Map<Integer, Integer> getMapOfSpeciesFrequencies() {
        return mapOfSpeciesFrequencies;
    }

    /**
     * @return the mapOfScansFrequencies
     */
    public Map<Integer, Integer> getMapOfScansFrequencies() {
        return mapOfScansFrequencies;
    }

    public void prettyPrint(int depth) {
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).getNode().getValue().compareTo(String.valueOf(LEAF)) != 0) {
                System.out.println(children.get(i).stringValue + " >>> " + children.get(i).getNode().getValue() + " = " + children.get(i).countAnalysisLeaves());

                for (int j = 0; j < depth; j++) {
                    System.out.print("  ");
                }
                children.get(i).prettyPrint(depth + 1);
            }
        }

    }
}
