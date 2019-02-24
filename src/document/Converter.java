package document;

import chain.Chain;
import chain.ChainImpl;
import chain.Location;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Converter {
    public List<Chain> unpack(String source) {
        List<Chain> chains = new ArrayList<>();
        List<String> list = Arrays.asList(source.split("\n"));
        int chainsNumber = Integer.getInteger(list.get(0));
        for (int i = 1; i < chainsNumber - 1; i++) {
            List<String> list1 = Arrays.asList(list.get(i).split(" "));
            Chain temp = new ChainImpl(list.get(0), new Color(
                    Integer.getInteger(list1.get(1)),
                    Integer.getInteger(list1.get(2)),
                    Integer.getInteger(list1.get(3))
            ));
            int part = Integer.getInteger(list.get(4));
            int location = part + Integer.getInteger(list.get(4));
            part += i;
            location += i;
            i++;
            List<Location> locations = new ArrayList<>();
            List<List<String>> parts = new ArrayList<>();
            for (; i < part; i++) {

            }
        }
        return chains;
    }

    public String pack(List<Chain> chains) {
        StringBuilder sb = new StringBuilder();
        sb.append(chains.size()).append('\n');
        chains.forEach(chain -> chain.pack(sb));
        return sb.toString();
    }
}
