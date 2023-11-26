/**
 * WTFException wraps more specific exceptions so the source code will be more readable.
 * It's a dirty hack and should not be done in real projects.
 */
class WTFException extends Exception {
    private String message;

    public WTFException(String message){
        this.message = message;
    }

    public WTFException(){
        this.message = "(no information provided)";
    }
    public void giveInformation() {
        System.out.println("WTF Exception: "+message+System.lineSeparator()+"Stack Trace:");
        printStackTrace();
    }
}
