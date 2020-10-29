package record;

public interface TreeNodeVisitor<E extends Exception> {
    void visit(Directory node) throws E;

    void visit(File node) throws E;

    void visit(SymbolicLink node) throws E;
}
