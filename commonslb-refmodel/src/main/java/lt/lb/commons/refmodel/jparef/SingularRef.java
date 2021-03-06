/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.refmodel.jparef;

import java.util.Arrays;
import java.util.regex.Matcher;
import javax.persistence.criteria.*;
import lt.lb.commons.parsing.StringOp;
import lt.lb.commons.refmodel.Ref;
import lt.lb.commons.refmodel.RefCompiler;

/**
 *
 * @author laim0nas100
 */
public class SingularRef<T> extends Ref<T> {
    
    public Path<T> getPathFrom(Path p) {
        String str = this.get();
        String[] split = StringOp.split(str, RefCompiler.separator);
        for (String path : split) {
            p = p.get(path);
        }
        return p;
    }

    public <A,B> Fetch<A,T> fetch(FetchParent<A,B> root, JoinType jt) {
        return root.fetch(local, jt);
    }

    public <A,B> Fetch<A,T> fetch(FetchParent<A,B> root) {
        return this.fetch(root, JoinType.INNER);
    }
}
