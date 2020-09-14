package record;

public interface TreeVisitor<E extends Exception> {
    public abstract void visitEnter(Directory node) throws E;

    public abstract void visitLeave(Directory node) throws E;

    public abstract void visit(File node) throws E;

    public abstract void visit(SymbolicLink node) throws E;
}
