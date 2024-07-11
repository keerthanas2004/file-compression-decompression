import java.io.*;
import java.util.*;

class HuffmanNode implements Comparable<HuffmanNode> 
{
    char data;                         //CHaracter
    int freq;                          //Frequency
    HuffmanNode left, right;           //Branches from the node

    HuffmanNode(char data, int freq) 
    {
        this.data = data;
        this.freq = freq;
        this.left = this.right = null;
    }

    @Override
    public int compareTo(HuffmanNode node) 
    {
        return this.freq - node.freq;
    }
}

class HuffmanTree 
{
    private HuffmanNode root;
    private Map<Character, String> huffmanCodes;

    public HuffmanTree(Map<Character, Integer> freqMap) 
    {
        PriorityQueue<HuffmanNode> minHeap = new PriorityQueue<>();
        for (Map.Entry<Character, Integer> entry : freqMap.entrySet()) 
        {
            minHeap.add(new HuffmanNode(entry.getKey(), entry.getValue()));                 //Adding the characters and their frequency to minHeap
        }

        while (minHeap.size() > 1) 
        {
            HuffmanNode left = minHeap.poll();
            HuffmanNode right = minHeap.poll();
            HuffmanNode newNode = new HuffmanNode('\0', left.freq + right.freq);       //Calculate the new heap
            newNode.left = left;
            newNode.right = right;
            minHeap.add(newNode);                                                           //Add the new heap to min heap
        }

        root = minHeap.poll();
        huffmanCodes = new HashMap<>();                                                     //Create a new hash map to store the huffman codes
        CreateHuffmanCodes(root, "");
    }

    private void CreateHuffmanCodes(HuffmanNode node, String code) 
    {
        if (node == null) 
            return;
        if (node.left == null && node.right == null) 
        {
            huffmanCodes.put(node.data, code);
        }

        CreateHuffmanCodes(node.left, code + "0");                                      //If node is on the left, the branch is a 0
        CreateHuffmanCodes(node.right, code + "1");                                     //If node is on the right, the branch is a 1
    }

    public Map<Character, String> getHuffmanCodes() 
    {
        return huffmanCodes;            //To fetch the Huffman Codes 
    }

    public HuffmanNode getRoot() 
    {
        return root;                    //To fetch the root of the Huffman Tree
    }
}

class HuffmanCompression                //For file compression
{

    public static void compressFile(String inputFile, String outputFile) throws IOException 
    {
        String content = readFile(inputFile);                                                       //Read the file
        Map<Character, Integer> freqMap = buildFrequencyMap(content);                               //Mark the frequency of each character present
        HuffmanTree huffmanTree = new HuffmanTree(freqMap);                                         //Create a Huffman Tree using the frequency map
        Map<Character, String> huffmanCodes = huffmanTree.getHuffmanCodes();                        //Mark the Huffman code for each character present

        StringBuilder encodedData = new StringBuilder();                                            //Create a new string to store encoded data
        for (char c : content.toCharArray()) 
        {
            encodedData.append(huffmanCodes.get(c));                                                //Append Huffman Code of each character to the encoded data
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputFile)))     
        {                                                                                           //Create a file to store the encoded data
            oos.writeObject(freqMap);                       //Frequency Map of the characters is written in the file
            oos.writeObject(encodedData.toString());        //Encoded data is written in the file
        }
    }

    private static String readFile(String filePath) throws IOException 
    {
        StringBuilder data = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) 
        {
            String line;
            while ((line = br.readLine()) != null) 
            {
                data.append(line).append("\n");                                          //Reads content of the file
            }
        }
        return data.toString();
    }

    private static Map<Character, Integer> buildFrequencyMap(String content) 
    {
        Map<Character, Integer> freqMap = new HashMap<>();
        for (char c : content.toCharArray()) 
        {
            freqMap.put(c, freqMap.getOrDefault(c, 0) + 1);                     //Calculates frequency of each character using hash map
        }
        return freqMap;
    }
}

class HuffmanDecompression                  //For file decompression
{

    public static void decompressFile(String inputFile, String outputFile) throws IOException, ClassNotFoundException 
    {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(inputFile))) 
        {
            @SuppressWarnings("unchecked")
            Map<Character, Integer> freqMap = (Map<Character, Integer>) ois.readObject();       //Fetch the frequency map from the file
            String encodedData = (String) ois.readObject();                                     //Fetch the encoded data fromm the file

            HuffmanTree huffmanTree = new HuffmanTree(freqMap);                                 //Create a Huffman Tree using the frequency map
            HuffmanNode root = huffmanTree.getRoot();                                           //Fetch the root of the Tree

            StringBuilder decodedData = new StringBuilder();                                    //Create a new string to store the decoded data
            HuffmanNode current = root;                                                         //Start at the root
            for (char bit : encodedData.toCharArray()) 
            {
                if (bit == '0')             //If encoded data is 0, go left
                {
                    current = current.left;
                } 
                
                else                        //If encoded data is 1, go right
                {
                    current = current.right;
                }

                if (current.left == null && current.right == null)              //If there's no left or right nodes, we have reached the base nodes
                {
                    decodedData.append(current.data);                           //The character stored in the node is appended into the decoded file
                    current = root;
                }
            }

            writeFile(outputFile, decodedData.toString());
        }
    }

    private static void writeFile(String filePath, String content) throws IOException 
    {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) 
        {
            bw.write(content);
        }
    }
}

public class Main {
    public static void main(String[] args) {
        try 
        {
            String inputFile = "sample_check.txt";
            String compressedFile = "compressed_file.huff";
            String decompressedFile = "decompressed_file.txt";

            HuffmanCompression.compressFile(inputFile, compressedFile);                     //To compress a file
            System.out.println("File compressed to " + compressedFile);                     

            HuffmanDecompression.decompressFile(compressedFile, decompressedFile);          //To decompress a file
            System.out.println("File decompressed to " + decompressedFile);
        } 
        catch (IOException | ClassNotFoundException e) 
        {
            e.printStackTrace();
        }
    }
}
