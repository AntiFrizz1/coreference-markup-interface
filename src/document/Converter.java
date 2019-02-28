package document;

import chain.Blank;
import chain.Chain;
import chain.ChainImpl;
import chain.Location;
import chain.Phrase;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class Converter {
    public List<Chain> unpack(String source) {
        List<Chain> chains = new ArrayList<>();
        List<String> list = Arrays.asList(source.split("\n"));
        int chainsNumber = Integer.valueOf(list.get(0));
        int i = 1;
        while (chainsNumber > 0) {
            List<String> list1 = Arrays.asList(list.get(i).split(" "));

            Chain temp = new ChainImpl(list1.get(0), new Color(
                    Integer.valueOf(list1.get(2)),
                    Integer.valueOf(list1.get(3)),
                    Integer.valueOf(list1.get(4))),
                    Integer.valueOf(list1.get(1)));

            i++;
            List<String> reprs = Arrays.asList(list.get(i++).split(" -- "));

            int k = i + Integer.valueOf(list1.get(4));

            for (int j = 0; i < k; i++) {
                String tempStr = list.get(i);
                Location location;
                if (tempStr.contains("Phrase")) {
                    location = new Phrase(reprs.get(j++), new HashSet<>(
                            Arrays.stream(tempStr.substring(8).split(" ")).
                                    map(Integer::valueOf).
                                    collect(Collectors.toList())
                    ));
                } else {
                    location = new Blank(Integer.valueOf(tempStr.substring(7)));
                }
                temp.addPart(location);
            }
            chains.add(temp);
            chainsNumber--;
        }
        return chains;
    }

    public String pack(List<Chain> chains) {
        StringBuilder sb = new StringBuilder();
        sb.append(chains.size()).append('\n');
        chains.forEach(chain -> chain.packSB(sb));
        return sb.toString();
    }
}
