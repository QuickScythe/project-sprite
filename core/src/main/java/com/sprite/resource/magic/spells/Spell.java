package com.sprite.resource.magic.spells;


import com.sprite.resource.magic.elements.Element;

public interface Spell {

    Element[] elements();

    String name();

    default boolean matches(Element[] elements) {
        //return true if all the elements in this spell are in the elements array
        for (Element element : elements()) {
            boolean found = false;
            for (Element e : elements) {
                if (e == element) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }
}
