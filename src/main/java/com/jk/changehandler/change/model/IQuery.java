package com.jk.changehandler.change.model;

import java.util.List;

/**
 * Created by jshridha on 10/17/15.
 */
public interface IQuery<T> {
    public List<T> execute() throws Exception;
}
