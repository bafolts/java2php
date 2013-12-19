package PLACEHOLDERS;

public abstract class HttpServlet {

	protected void doGet(HttpServletRequest Request, HttpServletResponse Response) {
		// This should be overriden.
	}

	public void service(HttpServletRequest Request, HttpServletResponse Response) {
		// This services the requests and responses, PHP will have this defined.
	}
	
}