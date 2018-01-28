package com.fonoster.sipio.core;

import javax.sip.Transaction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ContextStorage {

    List<Context> storage = new ArrayList();

    public List<Context> getStorage() {
        return storage;
    }

    public void saveContext(Context context) {
        storage.add(context);
    }

    public Context findContext(Transaction trans) {
        for (Context context : storage) {
            if (context.clientTransaction == trans ||
                    context.serverTransaction == trans) {
                return context;
            }
        }
        return null;
    }

    public boolean removeContext(Transaction trans) {
        Iterator<Context> iterator = storage.iterator();
        while (iterator.hasNext()) {
            Context context = iterator.next();
            if (context.clientTransaction == trans ||
                    context.serverTransaction == trans) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

}


