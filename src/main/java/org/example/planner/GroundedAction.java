package org.example.planner;

import org.example.ast.ActionNode;
import org.example.ast.ParameterNode;

import java.util.Map;
import java.util.Objects;
import java.util.*;

// Creating the grounded action
public class GroundedAction {
    private final ActionNode schema;
    private final Map<String, String> binding; // Parameter name bound to concrete object name. "r bound to rob1" as an example

    public GroundedAction(ActionNode schema, Map<String, String> binding) {
        this.schema = schema;
        LinkedHashMap<String, String> ordered = new LinkedHashMap<>();
        for (ParameterNode param : schema.getParameters()) {
            if (binding.containsKey(param.getName())) {
                ordered.put(param.getName(), binding.get(param.getName()));
            }
        }
        this.binding = Collections.unmodifiableMap(ordered);
        //this.binding = Map.copyOf(binding);
    }

    public ActionNode getSchema() {
        return schema;
    }

    public Map<String, String> getBinding() {

        return binding;
    }

    @Override
    public String toString() {
        return schema.getName() + binding; // fx move{r=rob, dest=A}
    }

    @Override  //se lige på den her, tror den skal slettes og fixes et andet sted henne
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroundedAction)) return false;
        GroundedAction ga = (GroundedAction) o;
        return schema.getName().equals(ga.schema.getName())
                && Objects.equals(binding, ga.binding);
    }


    @Override
    public int hashCode() {
        return Objects.hash(schema.getName(), binding);
    }
}
