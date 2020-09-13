package record;

public interface TreeVisitor {
    public abstract void visitEnter(Directory node);

    public abstract void visitLeave(Directory node);

    public abstract void visit(File node);

    public abstract void visit(SymbolicLink node);
}
