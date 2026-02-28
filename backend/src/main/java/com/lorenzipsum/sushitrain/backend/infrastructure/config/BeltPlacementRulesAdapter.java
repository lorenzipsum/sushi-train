package com.lorenzipsum.sushitrain.backend.infrastructure.config;

import com.lorenzipsum.sushitrain.backend.application.belt.BeltPlacementRules;
import org.springframework.stereotype.Component;

@Component
public class BeltPlacementRulesAdapter implements BeltPlacementRules {
    private final BeltPlacementProperties props;

    public BeltPlacementRulesAdapter(BeltPlacementProperties props) {
        this.props = props;
    }

    @Override
    public int minEmptySlotsBetweenNewPlates() {
        return props.minEmptySlotsBetweenNewPlates();
    }
}
