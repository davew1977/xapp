/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.framework;

import java.io.Serializable;

/**
 * Encapsulates an object that knows how to serialize/deserialize itself as a String using a LISP like format
 * the characters ',' (comma) and  '[]' (square brackets) are special
 * Object graphs are serialized as a list of primitives and lists.
 * e.g. given the following schema
 * <pre>Person
 *    {
 *       String name;
 *       List<Pet> pets;
 *    }
 *
 * Pet
 *    {
 *        String animal;
 *        List<String> toys;
 *        String name;
 *    }</pre>
 * with instance:
 * <pre>Person  {
 *    name: John
 *    pets: {
 *       Pet {
 *          animal: dog
 *          toys: ball, fake bone, dolly
 *       }
 *       Pet {
 *          animal: cat
 *          toys: ball of wool
 *       }
 *       Pet {
 *          animal: goldfish
 *          toys: pirate ship, stones
 *       }
 *    }
 * }
 * </pre>
 *
 * the string serialized form would be
 * <pre>[John,[[dog,[ball, fake bone, dolly]],[cat,[ball of wool]],[goldfish,[pirate ship, stones]]]]</pre>
 */
public interface StringSerializable extends Serializable
{
    /**
     * @param str the object as string
     */
    Object deserialize(String str);

    /**
     * @return object as string
     */
    String serialize();
}