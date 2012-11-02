package translator;

import java.util.ArrayList;

public class For extends FileTranslator {

	private ForArgument m_aArguments = null;
	private BlockBody m_bBlock = null;

	public For(String line) {
		line = line.trim();
		setContents(line);
		setPosition(getPosition()+3);
		
		m_aArguments = new ForArgument(getAndSetNextParen());

	}

	public ForArgument getArguments() {
		return m_aArguments;
	}
	
	public BlockBody getBlockBody() {
		return m_bBlock;
	}
	
	public void setBlockBody(BlockBody b) {
		m_bBlock = b;
	}

}