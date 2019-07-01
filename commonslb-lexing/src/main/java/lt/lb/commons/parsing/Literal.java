/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.parsing;

/**
 *
 * @author laim0nas100
 */
public class Literal extends Token {

    public final String value;

    public Literal(String id, Integer[] pos, String value) {
        super(id, pos);
        this.value = value;
    }

    @Override
    public String toString() {
        return super.toString() + this.value;
    }

}