package k8s.example.client.models;

public class CommandExecOut {
	private String cmdStdOut = null;
	private String cmdStdErr = null;
	private int cmdExitCode = 0;
	
	public String getCmdStdOut() {
		return cmdStdOut;
	}
	public void setCmdStdOut(String cmdStdOut) {
		this.cmdStdOut = cmdStdOut;
	}
	public String getCmdStdErr() {
		return cmdStdErr;
	}
	public void setCmdStdErr(String cmdStdErr) {
		this.cmdStdErr = cmdStdErr;
	}
	public int getCmdExitCode() {
		return cmdExitCode;
	}
	public void setCmdExitCode(int cmdExitCode) {
		this.cmdExitCode = cmdExitCode;
	}
}
