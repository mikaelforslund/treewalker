package org.codegen;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.codegen.tree.TreeNode;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 * Hello world!
 */
public final class App {

    private App() throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File("./diagram.drawio"));

        List<Node> list = document.selectNodes("//mxCell");
        List<Node> objectCellList = document.selectNodes("//object/mxCell");

        List<CNode> cNodeList = list.stream().map(n -> createCNode(n)).collect(Collectors.toList());
        cNodeList.addAll(objectCellList.stream().map(n -> createCNode(n)).collect(Collectors.toList()));

        TreeNode<CNode> root = null;
        for(CNode cnode : filterAndSort(cNodeList)) {
            TreeNode<CNode> treeNode = null;
            System.out.println(String.format("%s", cnode));

            if(root == null) {
                root = new TreeNode<CNode>(cnode);
            } else {
                treeNode = root.findTreeNode(new SearchCriteria(new CNode(cnode.getParent())));
                if(treeNode != null)
                    treeNode.addChild(cnode);
            }           
        }

        for (TreeNode<CNode> node : root) {
			String indent = createIndent(node.getLevel());
			System.out.println(indent + node.data);
		}
    }

    /**
     * Says hello to the world.
     * @param args The arguments of the program.
     * @throws DocumentException
     */
    public static void main(String[] args) throws DocumentException {
        new App();
    }

    private String createIndent(int depth) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < depth; i++) {
			sb.append(' ');
		}
		return sb.toString();
	}

    private CNode createCNode(Node node) { 
        // if this is an object/mxCell path then flatten them together...
        if(node.getPath().contains("/object/")) {
            return new CNode(node.valueOf("../@id"), node.valueOf("@label"), "", node.valueOf("@parent"));
        }

        return new CNode(node.valueOf("@id"), node.valueOf("@value"), "" /*node.valueOf("@style")*/, node.valueOf("@parent"));
    }

    private List<CNode> filterAndSort(List<CNode> list) { 
        Comparator<CNode> cmp = (CNode o1, CNode o2) -> {
            if(StringUtils.isEmpty(o1.getParent()) || StringUtils.isEmpty(o2.getParent()))
                return -1;

            String _id1[]  = o1.getParent().split("-");  // typically in the form nSJpXCImBmSawhCvZI22-10
            String _id2[]  = o2.getParent().split("-");

            String id1 = _id1.length > 1? _id1[1] : o1.getParent();  // either nSJpXCImBmSawhCvZI22-10 or 1
            String id2 = _id2.length > 1? _id2[1] : o2.getParent();

            return Integer.valueOf(id1).compareTo(Integer.valueOf(id2));
        };

        return list.stream().filter(n -> StringUtils.isNoneEmpty(n.getId())).sorted(cmp).collect(Collectors.toList());
    }
}

class SearchCriteria implements Comparable<CNode> {            
    private CNode searchNode;

    public SearchCriteria(CNode searchNode) {
        this.searchNode = searchNode;
    }

    @Override
    public int compareTo(CNode cnode) {
        if (cnode == null)
            return 1;
        boolean nodeOk = cnode.getId().equals(searchNode.getId());
        return nodeOk ? 0 : 1;
    }

    public CNode getSearchNode() {
        return searchNode;
    }
}
