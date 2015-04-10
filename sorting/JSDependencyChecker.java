package week1;

import com.sun.deploy.util.StringUtils;

import java.util.*;


/**
 * Created by dean on 3/30/15.
 */

/*
    We annotate the top of our JavaScript files with directives like "require('otherfile.js'); require('otherfile2.js');" to indicate dependencies between JS files. Write code that print out script file name in the order that if A require on B, print A before B.

    Please focus on finding the proper order, not on parsing the require statements or loading files. Assume you are given input:
             - dependencies: a map from file name to an array of the names of files
    A
    / \
    B <- C
    Map : A -> B,C
              C->B
    You should print in the order A, C, B
*/

public class JSDependencyChecker {

    //Using DFS to go to the leave
    public static void loadDependency(String filename, Map<String, List<String>> dependencyMap, Stack<String> printOrderStack, Set<String> alreadyVisited ) {

        if(filename == null || filename.length()==0)
            return;

        //get the dependencies children
        List<String> dependencies = dependencyMap.get(filename);

        if (dependencies != null && !dependencies.isEmpty() && !alreadyVisited.contains(filename)) {
            alreadyVisited.add(filename);  //Prevent circular reference A->B, B->C, C->A

            for (String dependency : dependencies) {

                //DFS to each children
                loadDependency(dependency, dependencyMap, printOrderStack, alreadyVisited);

            }
        }

        //insert to printOrderStack only if this is not being inserted already by the DFS
        //usually the parent of the leaves
        if(printOrderStack.search(filename) < 0 && (filename != null && filename.length()>0))
            printOrderStack.push(filename);

    }

    public static void main(String args[]) {


        Map<String, List<String>> DM = new HashMap<String, List<String>>();
        List<String> DL1 = new ArrayList<String>(2);
        DL1.add("C");
        DL1.add("B");
        DL1.add("D");
        DM.put("A", DL1);

        List<String> DL2 = new ArrayList<String>(1);
        DL2.add("B");
        DL2.add("D");
        DM.put("C", DL2);

        List<String> DL3 = new ArrayList<String>(1);
        DL3.add("B");
        DM.put("D", DL3);

        Stack<String> printOrderStack = new Stack<String>();

        loadDependency("A", DM, printOrderStack, new HashSet<String>());

        //Print out A C D B
        while(!printOrderStack.empty()) {
            System.out.print(printOrderStack.pop() + " ");
        }
    }
}
