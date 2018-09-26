/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.refmodel.jparef;

import javax.persistence.criteria.*;

/**
 *
 * @author laim0nas100
 */
public class CollectionRef<T> extends SingularRef<T> {

    public <E> CollectionJoin<E, T> join(Root<E> root) {
        return this.join(root, JoinType.INNER);
    }

    public <E> CollectionJoin<E, T> join(Root<E> root, JoinType jt) {
        return root.joinCollection(this.local, jt);
    }

}
