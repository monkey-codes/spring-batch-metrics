package codes.monkey.batchstats

import java.util.stream.Collectors

/**
 * @author Johan Zietsman (jzietsman@thoughtworks.com.au).
 */
class StatsNamespace {

    Deque<String> stack = new ArrayDeque<>()
    String currentNamespace = ""
    StatsNamespace parent

    StatsNamespace(StatsNamespace parent) {
        this.parent = parent
    }

    StatsNamespace push(String name) {
        stack.push(name)
        updateCurrent()
    }

    private StatsNamespace updateCurrent() {
        //TODO improve the reverse thingy
        if (stack.isEmpty() && parent) {
            currentNamespace = parent.name()
            return this
        }
        def tempNamespace = new ArrayList<>(stack).reverse().stream().collect(Collectors.joining("."))
        if (parent && !parent.isEmpty()) {
            currentNamespace = parent.name() + "." + tempNamespace
        } else {
            currentNamespace = tempNamespace
        }
        this
    }

    boolean isEmpty() {
        stack.isEmpty()
    }

    StatsNamespace pop() {
        stack.pop()
        updateCurrent()
    }


    String name() {
        currentNamespace
    }

    @Override
    String toString() {
        currentNamespace
    }
}
