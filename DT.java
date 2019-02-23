import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;


public class praveen_assign13 {

    private static String INPUT_FILE = "restaurant.csv";

    enum Attr
    {
        ALTERNATE, BAR, FRI_SAT, HUNGRY, PATRONS, PRICE, RAINING, RESERVATION, TYPE, WAIT_ESTIMATE, WILL_WAIT;
    }

    //Input read.
    private Map<Attr, List<String>> readInput() throws Exception {
        Map<Attr, List<String>> dataSet = new HashMap<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(INPUT_FILE)));
        String inputLine =  null;
        while( (inputLine = br.readLine()) != null) {
            String Data[] =  inputLine.split(",");
            int i = 0;
            for(Attr f : Attr.values()) {
                if(!dataSet.containsKey(f)) {
                    dataSet.put(f, new ArrayList<>());
                }
                dataSet.get(f).add(Data[i].trim());
                i++;
            }
        }
        return dataSet;

    }

    //Loops in CSV rows
    private List<Integer> rowList(List<String> strList, String value) {
        List<Integer> rowList = new ArrayList<>();
        int i = 0;
        for(String str : strList) {
            if(str.equals(value)) {
                rowList.add(i);
            }
            i++;
        }
        return rowList;
    }

    //Plurality value calculation.
    private String PValue(List<String> domainValues) {
        Random rand = new Random();
        Map<String,Integer> countMap = new HashMap<>();
        for(String str : domainValues) {
            if(!countMap.containsKey(str)) {
                countMap.put(str, 0);
            }
            countMap.put(str, countMap.get(str) +1);
        }
        int max = -1;
        String retStr = null;
        for(Map.Entry<String, Integer> entry : countMap.entrySet()) {
            if(max == entry.getValue()) {
                retStr =  rand.nextBoolean() ? entry.getKey() : retStr;
            }
            if(max < entry.getValue()) {
                max  = entry.getValue();
                retStr = entry.getKey();
            }
        }
        return  retStr;
    }

    //Finds successors based on info gain calc
    private Tree buildTree(Map<Attr, List<String>> dataSet) {
        System.out.println(" new Iteration");
        // recursive terminal statement when no attribute is present.
        if(dataSet.size() == 1) {
            Tree tree = new Tree(PValue(dataSet.get(Attr.WILL_WAIT)),true);
            return tree;
        }

        if(allClear(dataSet.get(Attr.WILL_WAIT))) {
            Tree tree = new Tree(dataSet.get(Attr.WILL_WAIT).get(0),true);
            return tree;
        }


        Double maxInfoGain = -Double.MAX_VALUE;
        double targetEntropy = Entropy(dataSet.get(Attr.WILL_WAIT));
        Attr maxGainAttr = null;
        for(Attr f : dataSet.keySet()) {
            if(f.equals(Attr.WILL_WAIT)) {
                continue;
            }
            Double infoGain_i = InfoGain(targetEntropy, dataSet.get(f), dataSet.get(Attr.WILL_WAIT));
            System.out.println(f + " " + infoGain_i);
            if(maxInfoGain == null || maxInfoGain < infoGain_i) {
                maxGainAttr = f;
                maxInfoGain = infoGain_i;
            }
        }
        Tree root = new Tree(maxGainAttr.toString(), false) ;
        Set<String> featureDomain = new HashSet<>(dataSet.get(maxGainAttr));
        for(String domainValue : featureDomain) {

            Map<Attr, List<String>> filteredDataSet = duplicate(dataSet);
            List<Integer> rowIndex = rowList(dataSet.get(maxGainAttr), domainValue);
            subSet(filteredDataSet, rowIndex);
            filteredDataSet.remove(maxGainAttr);

            Tree tree =  buildTree( filteredDataSet);
            root.getChildNodes().put(domainValue, tree);
        }

        return root;
    }

    //dataset pruning
    private void subSet(Map<Attr, List<String>> dataSet, List<Integer> rowList) {
        for(Attr f : dataSet.keySet()) {
            List<String> filteredValues = new ArrayList<>();
            for(Integer row : rowList) {
                filteredValues.add(dataSet.get(f).get(row));
            }
            dataSet.put(f,filteredValues);
        }
    }

    private boolean allClear(List<String> resultList) {
        String str = resultList.get(0);
        for (String compareStr : resultList) {
            if (!compareStr.equals(str))
                return false;
        }
        return true;
    }

    public  void run() throws Exception {
        Map<Attr, List<String>> dataSet = readInput();
        Tree root = buildTree(dataSet);
        System.out.println("");
        getOutput(null, root, 0);
        System.out.println("");
    }

    //Entropy calculation.
    private double Log2(double value) {
        return Math.log(value) / Math.log(2);
    }

    //calculates infogain at every level
    private double InfoGain(double rootEntropy, List<String> featureValues, List<String> targetFeatureValues) {
        Set<String> domainValues = new HashSet<>(featureValues);
        Double totalEntropy = 0.0;
        int size = featureValues.size();
        for(String domain : domainValues) {
            List<String> targetSubset = new ArrayList<>();
            int domainCount = 0;
            int i = 0;
            for(String str : featureValues) {
                if(str.equals(domain)) {
                    targetSubset.add(targetFeatureValues.get(i));
                    domainCount++;
                }
                i++;
            }
            double domainEntropy = domainCount/(double)size * Entropy(targetSubset);
            totalEntropy += domainEntropy;
        }
        return  rootEntropy - totalEntropy;
    }

    // sum of -P * log2(P)
    private double Entropy(List<String> featureValues) {
        Map<String, Integer> valueCountMap = new HashMap<>();

        for(String str : featureValues ) {
            if(!valueCountMap.containsKey(str)) {
                valueCountMap.put(str,0);
            }
            valueCountMap.put(str, valueCountMap.get(str) + 1);
        }
        int size = featureValues.size();
        double entropy = 0.0;
        for(Map.Entry<String, Integer> valueCountEntry : valueCountMap.entrySet()) {
            double prob = valueCountEntry.getValue() /(double)size;
            double valueEntropy = -prob*Log2(prob);
            entropy += valueEntropy;
        }
        return entropy;
    }

    //Copy map-map
    private Map<Attr,List<String>> duplicate(Map<Attr, List<String>> featureListMap) {
        Map<Attr,List<String>> featureMap = new HashMap<>();
        for(Map.Entry<Attr, List<String>> entry : featureListMap.entrySet()) {
            featureMap.put(entry.getKey(), duplicate(entry.getValue()));
        }
        return featureMap;
    }

    //Copy list-list
    private List<String> duplicate(List<String> strList) {
        List<String> arr = new ArrayList<>();
        for(String str: strList) {
            arr.add(str);
        }
        return arr;
    }

    //Tree structure Output
    private void getOutput(String domainValue, Tree tree, int spacing) {
        System.out.print("\n");
        for(int i = 0; i<spacing-1; i++) {
            System.out.print("+\t");
        }
        if(spacing > 0)
            System.out.print("+---");
        if(domainValue !=null) {
            System.out.print(  domainValue + " >>>>>");
        }
        System.out.print(tree.getData());
        for(Map.Entry<String, Tree> entry : tree.getChildNodes().entrySet()) {
            getOutput(entry.getKey(), entry.getValue(), spacing + 1);
        }
    }

    public static void main(String arg[]) throws Exception {
        praveen_assign13 pchand = new praveen_assign13();
        pchand.run();

    }

    //TreeNode class definition.
    class Tree {
        private String data;
        //    private String data;
        private boolean isPureDataSet;
        private Map<String, Tree> childNodes;

        public Tree(String data, boolean isPureDataSet) {
            this.data = data;
            this.isPureDataSet = isPureDataSet;
            childNodes = new HashMap<>();
        }

        public String getData() {
            return data;
        }

        public Map<String, Tree> getChildNodes() {
            return childNodes;
        }

        @Override
        public String toString() {
            return data +  " "  + isPureDataSet;
        }
    }

}