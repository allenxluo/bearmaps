package bearmaps.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Trie {

    Node root;

    public Trie() {
        root = new Node(' ', false);
    }

    public void add(String key) {
        if (key == null || key.length() < 1) {
            return;
        }
        Node curr = root;
        for (int i = 0, n = key.length(); i < n; i++) {
            char c = key.charAt(i);
            if (!curr.map.containsKey(c)) {
                curr.map.put(c, new Node(c, false));
            }
            curr = curr.map.get(c);
        }
        curr.isKey = true;
        curr.numKeys += 1;
    }

    public List<String> keysWithPrefix(String prefix) {
        List<String> returnList = new ArrayList<>();
        Node curr = root;
        for (int i = 0; i < prefix.length(); i++) {
            char c = prefix.charAt(i);
            if (!curr.map.containsKey(c)) {
                return returnList;
            }
            curr = curr.map.get(c);
        }
        keysWithPrefixHelper(returnList, curr, prefix);
        return returnList;
    }

    private void keysWithPrefixHelper(List list, Node node, String prefix) {
        if (node.isKey) {
            for (int i = 0; i < node.numKeys; i++) {
                list.add(prefix);
            }
        }
        if (!node.map.isEmpty()) {
            for (Map.Entry mapElement : node.map.entrySet()) {
                keysWithPrefixHelper(list, (Node) mapElement.getValue(), prefix + mapElement.getKey());
            }
        }
    }

    protected class Node {

        Character item;
        Boolean isKey;
        int numKeys;
        HashMap<Character, Node> map;

        public Node(Character item, Boolean isKey) {
            this.item = item;
            this.isKey = isKey;
            map = new HashMap<>();
        }
    }
}
