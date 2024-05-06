package manager;

import java.util.ArrayList;
import java.util.List;


import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import model.Bodega;
import model.Campo;
import model.Entrada;
import model.Vid;
import utils.TipoVid;

public class Manager {
	private static Manager manager;
	private ArrayList<Entrada> entradas;
	private Session session;
	private Transaction tx;
	private Bodega b;
	private Campo c;

	private Manager () {
		this.entradas = new ArrayList<>();
	}
	
	public static Manager getInstance() {
		if (manager == null) {
			manager = new Manager();
		}
		return manager;
	}
	
	private void createSession() {
		org.hibernate.SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
    	session = sessionFactory.openSession();
	}

	public void init() {
		createSession();
		getEntrada();
		manageActions();
		showAllCampos();
		showCantidadVidByTipo();
		showAllVids();
		session.close();
	}

	private void manageActions() {
		for (Entrada entrada : this.entradas) {
			try {
				System.out.println(entrada.getInstruccion());
				switch (entrada.getInstruccion().toUpperCase().split(" ")[0]) {
					case "B":
						addBodega(entrada.getInstruccion().split(" "));
						break;
					case "C":
						addCampo(entrada.getInstruccion().split(" "));
						break;
					case "V":
						addVid(entrada.getInstruccion().split(" "));
						break;
					case "#":
						vendimia();
						break;
					default:
						System.out.println("Instruccion incorrecta");
				}
			} catch (HibernateException e) {
				e.printStackTrace();
				if (tx != null) {
					tx.rollback();
				}
			}
		}
	}

	private void vendimia() {
	    tx = session.beginTransaction();
	    List<Bodega> bodegas = session.createQuery("from Bodega").list();
	    for (Bodega bodega : bodegas) {
	        List<Campo> campos = bodega.getCampos();
	        for (Campo campo : campos) {
	            bodega.getVids().addAll(campo.getVids());
	        }
	        session.save(bodega);
	    }
	    tx.commit();
	}




	private void addVid(String[] split) {
		Vid v = new Vid(TipoVid.valueOf(split[1].toUpperCase()), Integer.parseInt(split[2]));
		tx = session.beginTransaction();
		session.save(v);
		
		c.addVid(v);
		session.save(c);
		
		tx.commit();
		
	}

	private void addCampo(String[] split) {
		c = new Campo(b, null);
		tx = session.beginTransaction();
		
		int id = (Integer) session.save(c);
		c = session.get(Campo.class, id);
		
		tx.commit();
	}

	private void addBodega(String[] split) {
		b = new Bodega(split[1]);
		tx = session.beginTransaction();
		
		int id = (Integer) session.save(b);
		b = session.get(Bodega.class, id);
		
		tx.commit();
		
	}

	private void getEntrada() {
		tx = session.beginTransaction();
		Query q = session.createQuery("select e from Entrada e");
		this.entradas.addAll(q.list());
		tx.commit();
	}

	private void showAllCampos() {
		tx = session.beginTransaction();
		Query q = session.createQuery("select c from Campo c");
		List<Campo> list = q.list();
		for (Campo c : list) {
			System.out.println(c);
		}
		tx.commit();
	}
	
	private void showCantidadVidByTipo() {
	    tx = session.beginTransaction();
	    Query q = session.createQuery("select vid.vid, sum(vid.cantidad) from Vid vid group by vid.vid");
	    List<Object[]> results = q.list();
	    for (Object[] result : results) {
	        TipoVid tipoVid = (TipoVid) result[0];
	        int cantidad = ((Number) result[1]).intValue();
	        System.out.println("TipoVid: " + tipoVid + ", Cantidad: " + cantidad);
	    }
	    tx.commit();
	}

	
	private void showAllVids() {
	    tx = session.beginTransaction();
	    Query q = session.createQuery("from Vid vid order by (case when vid.vid = :negra then 1 else 2 end), vid.vid");
	    q.setParameter("negra", TipoVid.NEGRA);
	    List<Vid> list = q.list();
	    for (Vid vid : list) {
	        System.out.println(vid);
	    }
	    
	    q = session.createQuery("from Vid vid order by (case when vid.vid = :blanca then 2 else 1 end), vid.vid");
	    q.setParameter("blanca", TipoVid.BLANCA);
	    list = q.list();
	    for (Vid vid : list) {
	        System.out.println(vid);
	    }
	    tx.commit();
	}




	
}
