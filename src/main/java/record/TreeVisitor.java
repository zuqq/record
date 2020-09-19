package record;

public interface TreeVisitor<E extends Exception> {
    void visit(Directory node) throws E;

    void visit(Executable node) throws E;

    void visit(File node) throws E;

    void visit(SymbolicLink node) throws E;
}
