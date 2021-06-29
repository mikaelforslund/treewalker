package org.codegen;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CNode {
    private String id;
    private String value;
    private String style;
    private String parent;

    public CNode(String id) {
        this.id = id;
    }

    public boolean isEmpty() {
        return getId() == null || "".equals(getId());
    }
}
