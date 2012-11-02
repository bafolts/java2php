package translator;

import java.util.ArrayList;

public class Try extends FileTranslator {

	private BlockBody m_bBlock = null;
	private Try m_tParentTry = null;
	private ArrayList<Catch> m_aCatch = new ArrayList<Catch>(0);
	private Finally m_fFinally = null;

	public Try(String line) {
		setContents(line.trim().substring(3));
		m_bBlock = new BlockBody(getAndSetNextBlock());
	}
	
	public BlockBody getBlockBody() {
		return m_bBlock;
	}

	public void setParentTry(Try t) {
		m_tParentTry = t;
	}

	public Try getParentTry() {
		return m_tParentTry;
	}

	public ArrayList<Catch> getCatch() {
		return m_aCatch;
	}

	public void setFinally(Finally f) {
		m_fFinally = f;
	}

	public Finally getFinally() {
		return m_fFinally;
	}

}