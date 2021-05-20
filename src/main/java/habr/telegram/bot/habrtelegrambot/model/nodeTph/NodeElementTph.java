package habr.telegram.bot.habrtelegrambot.model.nodeTph;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
public class NodeElementTph extends NodeTph {

    private String tag;
    private Map<String, String> attrs;
    private List<NodeTph> children;

    public NodeElementTph(String tag) {
        this.tag = tag;
    }

    public void addAttr(String name, String value) {
        if (attrs == null) {
            attrs = new HashMap<>();
        }
        attrs.put(name, value);
    }

    public void addChildren(NodeTph nodeTph) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(nodeTph);
    }
}
