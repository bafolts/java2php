package translator;

public class Switch extends FileTranslator {

	private Argument m_aArgument = null;
	private BlockBody m_bBlock = null;

	public Switch(String line) {
		setContents(line.trim().substring(6));
		String s = getAndSetNextParen().trim();
		m_aArgument = new Argument(s.substring(1,s.length()-1));

	}

	public void setBlockBody(BlockBody b) {
		m_bBlock = b;
	}

	public Argument getArgument() {
		return m_aArgument;
	}
	
	public BlockBody getBlockBody() {
		return m_bBlock;
	}

}