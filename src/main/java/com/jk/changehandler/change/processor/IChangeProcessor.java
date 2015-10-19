package com.jk.changehandler.change.processor;

import com.amazonaws.services.kinesis.model.Record;

/**
 * interface for handling a 'change' event in the kinesis stream.
 * A change event is basically a database change (insert/modify/remove).
 *
 * A change processor takes that change event, and tells which `channels` should get the update for
 * that `change`. Each `channel` has a corresponding query. So, the processor checks if the
 * change satisfies that queries or not.
 *
 * For example query can be "all users in Delhi", now if a user changes his city from Mumbai to Bangalore
 * the channel corresponding to this query should not get any update.
 */
public interface IChangeProcessor<T> {
    public void process(T change);
}
