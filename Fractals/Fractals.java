

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jmge.gif.Gif89Encoder;

/**
 * Servlet implementation class Animation
 */
@WebServlet("/Animation")
public class Fractals extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
	public Fractals() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//Asking for input till all of the parameters are not null and the right type
		if (request.getParameter("s") == null || request.getParameter("i") == null ||
				!request.getParameter("s").matches("\\d+") || !request.getParameter("i").matches("\\d+")) {
			PrintWriter out = response.getWriter();
			response.setContentType("text/html");
			out.println("<form>");
			out.println("<h2>Simple fractals</h2>");
			out.println("<h3>Inputs</h3>");
			out.println("Side of square: <input name='s' value='500'/><br>");
			out.println("Type of fractal: <select name='t'><option value='S'>Sierpinski carpet</option>"
					+ "<option value='C'>Cantor</option><option value='V'>Vicsek</option><option value='T'>T-Square</option>"
					+ "</select><br>");
			out.println("Number of iterations: <input name='i' value='5'/><small>(1-5, for T-square 1-7)</small><br>");
			out.println("Speed of animation: <input name='a' value='5'/><small>(1 is the fastest)</small><br>");
			out.println("<input type='submit' value='Enter'/>");
			out.println("</form>");
		} else {
			try {
				//Storing & parsing the parameters
				int s = Integer.parseInt(request.getParameter("s"));
				int iteration = Integer.parseInt(request.getParameter("i")) - 1;
				String type = request.getParameter("t");
				int speed = Integer.parseInt(request.getParameter("a"));
				
				//Maps each point to the size of the side of the square
				Map<Point, Integer> sqr = new HashMap<>();
				
				//Defines the starting point(s) of each fractal to the map sqr
				if (type.substring(0,1).toUpperCase().equals("S"))
					sqr.put(new Point(s/3, s/3), s/3);
				else if (type.substring(0,1).toUpperCase().equals("T"))
					sqr.put(new Point(s/4, s/4), s/2);					
				else if (type.substring(0,1).toUpperCase().equals("C"))
					sqr.put(new Point(10, 10), s-20);
				else {
					sqr.put(new Point(0,0), s/3);
					sqr.put(new Point(s/3,s/3), s/3);
					sqr.put(new Point(0,s*2/3), s/3);
					sqr.put(new Point(s*2/3,0), s/3);
					sqr.put(new Point(s*2/3,s*2/3), s/3);
				}
				
				Gif89Encoder genc = new Gif89Encoder();
				
				//amount of iterations is user input multiplied by speed
				for (int i=0;i<=iteration*speed + speed - 1;i++){
					BufferedImage image = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = image.createGraphics();
					
					//draws background frame
					drawFrame(g, s, i);
					
					//Depending on the type of the fractal, calls the correct draw-method
					if (type.substring(0,1).toUpperCase().equals("C"))
						drawRect(g, sqr, s, iteration + 1);
					else
						drawSquare(g, sqr);
					
					genc.addFrame(image);
					g.dispose();
					
					//every x iterations the right method is called to change or add new
					//entries to map sqr
					if (i % speed == speed-1){
						if (type.substring(0,1).toUpperCase().equals("S"))
							sqr = sierpinski(sqr);
						else if (type.substring(0,1).toUpperCase().equals("T"))
							sqr = tSquare(sqr);
						else if (type.substring(0,1).toUpperCase().equals("C"))
							sqr = cantor(sqr, s, iteration + 1);
						else
							sqr = vicsek(sqr);
					}
				}
				genc.setUniformDelay(10);
				genc.setLoopCount(0);
				genc.encode(response.getOutputStream());
			}catch(Exception e){
				System.err.println(e);
			}	
		}
	}

	//Background
    static void drawFrame(Graphics2D g, int s, int i) {
    	g.setColor(Color.blue);
    	g.fillRect(0, 0, s, s);
    }

    //Drawing method for all but the Cantor fractal
    static void drawSquare(Graphics2D g, Map<Point, Integer> sqr) {
    	g.setColor(Color.white);
    	for (Point p : sqr.keySet()) {
    		g.fillRect(p.x, p.y, sqr.get(p), sqr.get(p));
    	}
    }
    
    //Drawing method for Cantor fractal
    static void drawRect(Graphics2D g, Map<Point, Integer> sqr, int s, int iter) {
    	g.setColor(Color.white);
    	for (Point p : sqr.keySet()) {
    		g.fillRect(p.x, p.y, sqr.get(p), s/iter - 10 - iter);
    	}
    }
    
    //Method for calculating squares for next iteration for T-Square fractal
    static Map<Point, Integer> tSquare(Map<Point, Integer> sqr) {
    	Map<Point, Integer> help = new HashMap<>();
    	help.putAll(sqr);
    	for (Point p : sqr.keySet()) {
    		help.put(new Point(p.x - sqr.get(p)/4, p.y - sqr.get(p)/4), sqr.get(p)/2);
    		help.put(new Point(p.x - sqr.get(p)/4, p.y + sqr.get(p)*3/4), sqr.get(p)/2);
    		help.put(new Point(p.x + sqr.get(p)*3/4, p.y - sqr.get(p)/4), sqr.get(p)/2);
    		help.put(new Point(p.x + sqr.get(p)*3/4, p.y + sqr.get(p)*3/4), sqr.get(p)/2);
    	}
    	return help;
    }

    //Method for calculating squares for next iteration for Sierpinski fractal
    static Map<Point, Integer> sierpinski(Map<Point, Integer> sqr) {
    	Map<Point, Integer> help = new HashMap<>();
    	help.putAll(sqr);
    	for (Point p : sqr.keySet()) {
    		help.put(new Point(p.x - sqr.get(p)*2/3, p.y - sqr.get(p)*2/3), sqr.get(p)/3);
    		help.put(new Point(p.x + sqr.get(p)/3, p.y - sqr.get(p)*2/3), sqr.get(p)/3);
    		help.put(new Point(p.x + sqr.get(p)*4/3, p.y - sqr.get(p)*2/3), sqr.get(p)/3);
    		help.put(new Point(p.x - sqr.get(p)*2/3, p.y + sqr.get(p)/3), sqr.get(p)/3);
    		help.put(new Point(p.x + sqr.get(p)/3, p.y + sqr.get(p)/3), sqr.get(p)/3);
    		help.put(new Point(p.x + sqr.get(p)*4/3, p.y + sqr.get(p)/3), sqr.get(p)/3);
    		help.put(new Point(p.x - sqr.get(p)*2/3, p.y + sqr.get(p)*4/3), sqr.get(p)/3);
    		help.put(new Point(p.x + sqr.get(p)/3, p.y + sqr.get(p)*4/3), sqr.get(p)/3);
    		help.put(new Point(p.x + sqr.get(p)*4/3, p.y + sqr.get(p)*4/3), sqr.get(p)/3);
    	}
    	return help;
    }
    
    //Method for calculating rectangles for next iteration for Cantor fractal
    static Map<Point, Integer> cantor(Map<Point, Integer> sqr, int s, int iter) {
    	Map<Point, Integer> help = new HashMap<>();
    	help.putAll(sqr);
    	for (Point p : sqr.keySet()) {
    		help.put(new Point(p.x, p.y + s/iter), sqr.get(p)/2 - sqr.get(p)/10);
    		help.put(new Point(p.x + sqr.get(p)/2 + sqr.get(p)/10, p.y + s/iter), sqr.get(p)/2 - sqr.get(p)/10);
    	}
    	return help;
    }
    
    //Method for calculating squares for next iteration for Vicsek fractal
    static Map<Point, Integer> vicsek(Map<Point, Integer> sqr) {
    	Map<Point, Integer> help = new HashMap<>();
    	for (Point p : sqr.keySet()) {
    		help.put(new Point(p.x, p.y), sqr.get(p)/3);
    		help.put(new Point(p.x + sqr.get(p)/3, p.y + sqr.get(p)/3), sqr.get(p)/3);
    		help.put(new Point(p.x + sqr.get(p)*2/3, p.y), sqr.get(p)/3);
    		help.put(new Point(p.x, p.y + sqr.get(p)*2/3), sqr.get(p)/3);
    		help.put(new Point(p.x + sqr.get(p)*2/3, p.y + sqr.get(p)*2/3), sqr.get(p)/3);
    	}
    	return help;
    }
}
