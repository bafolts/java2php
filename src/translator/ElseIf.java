package translator;

public class ElseIf extends FileTranslator {

	private Argument m_aArgument = null;
	private BlockBody m_bBlock = null;

	public ElseIf(String line) {
		line = line.trim();
		setContents(line);

		goToNext("(");
		setPosition(getPosition()-1);

		String s = getAndSetNextParen();
		s = s.substring(1,s.length()-1);
		
		m_aArgument = new Argument(s);

	}

	public Argument getArgument() {
		return m_aArgument;
	}
	
	public BlockBody getBlockBody() {
		return m_bBlock;
	}
	
	public void setBlockBody(BlockBody b) {
		m_bBlock = b;
	}

}