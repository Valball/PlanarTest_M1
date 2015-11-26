package mainpackage;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.sound.sampled.BooleanControl;

public class Graphe{

	protected int nb_sommets;
	protected Map<Integer, Sommet> sommets;
	protected List<Sommet> cycle;

	public List<Sommet> getCycle() {
		return cycle;
	}

	public Graphe(int n) {
		nb_sommets = n;
		sommets = new HashMap<Integer, Sommet>();
	}

	public Graphe(int n, Map<Integer, Sommet> g) {
		nb_sommets = n;
		sommets = g;
	}

	public int getNb_sommets() {
		return nb_sommets;
	}

	public void setNb_sommets(int nb_sommets){
		this.nb_sommets = nb_sommets;
	}

	public void setSommets(Map<Integer, Sommet> sommets){
		this.sommets = sommets;
	}

	public void setCycle(List<Sommet> cycle){
		this.cycle = cycle;
	}

	public Sommet getPremierSommet() {
		int firstKey = (Integer) sommets.keySet().toArray()[0];
		Sommet s = sommets.get(firstKey);
		return s;
	}

	public Sommet getSommetByNumero(int numSommet){
		Sommet s = null;
		Iterator it = sommets.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			s = (Sommet) pair.getValue();
			if(s.getNum_sommet() == numSommet){
				return s;
			}
			it.remove(); // avoids a ConcurrentModificationException
		}
		return null;
	}

	@Override
	public String toString() {
		String s = "";
		for (Sommet so : sommets.values()) {
			s += so + System.getProperty("line.separator");
		}
		return s;
	}

	public String toStringCycle() {
		String s = "";

		for (Sommet u : getCycle()) {
			s += u.getNum_sommet() + " ";
		}
		return s + System.getProperty("line.separator");
	}

	public void parcours_largeur(int dep, Predicate<Sommet> f) {
		ArrayDeque<Sommet> q = new ArrayDeque<Sommet>();
		cleanProperties();
		Sommet s_dep = sommets.get(dep);
		s_dep.setEtat(Etat.Traite);
		s_dep.setDistance(0);
		q.add(s_dep);
		Sommet peek;
		while (!q.isEmpty()) {
			peek = q.peek();
			for (Sommet sommet : peek.getVoisins()) {
				if (f.test(sommet)) {
					if (sommet.getEtat() == Etat.Non_Atteint) {
						sommet.setEtat(Etat.Atteint);
						sommet.setDistance(peek.getDistance() + 1);
						sommet.setPere(peek);
						q.add(sommet);
					}
				}
			}
			q.poll();
			peek.setEtat(Etat.Traite);
		}
	}

	private void cleanProperties() {
		for (Sommet sommet : sommets.values()) {
			sommet.cleanProperties();
		}
	}

	public void parcours_profondeur(int dep) {
		cleanProperties();
		visiter(dep);
	}

	private void visiter(int u) {
		Sommet s = sommets.get(u);
		s.setEtat(Etat.Atteint);
		for (Sommet sommet : s.getVoisins()) {
			if (sommet.getEtat() == Etat.Non_Atteint) {
				sommet.setPere(s);
				visiter(s.getNum_sommet());
			}
		}
		s.setEtat(Etat.Traite);
	}

	public boolean calculCycle(Sommet u) {
		// TODO : ludo, faudrait ne plus utilsier la list cycle mais le graphe h
		cycle = new ArrayList<Sommet>();
		cleanProperties();
		return calculCycleRec(u);
	}

	private boolean calculCycleRec(Sommet u) {
		u.setEtat(Etat.Atteint);
		cycle.add(u);
		List<Sommet> voisins = new ArrayList<Sommet>();
		//voisins = cloneList(u.getVoisins());//voisins est un clone de u.getVoisins
		voisins = u.getVoisins();
//		if (voisins.contains(u.getPere())) {
//			//int indexOfPere = voisins.indexOf(u.getPere());
//			int indexOfPere = u.getVoisins().indexOf(u.getPere());
//			voisins.remove(indexOfPere);// exclure le père du parcours
//			//cette ligne doit exclure le père de la liste voisins, mais pas de u.getVoisins
//		}
		for (Sommet v : voisins) {
			if(v != u.getPere()){
				if (v.getEtat() == Etat.Atteint) {
					cycle.add(v);// a ce stade, le même sommet doit etre présent
					// deux fois dans la liste
					// je vais tronquer la liste cycle et y laisse uniquement ce
					// qu'il y a entre ces deux sommets

					int firstIndex = cycle.indexOf(v);
					int lastIndex = cycle.lastIndexOf(v);
					cycle = cycle.subList(firstIndex, lastIndex + 1);


					return true;
				}
				if (v.getEtat() == Etat.Non_Atteint) {
					v.setPere(u);
					return calculCycleRec(v);
				}
			}
		}
		u.setEtat(Etat.Traite);
		cycle.clear();
		return false;
	}

	public void ajouterVoisins(int sommet, int... voisin) {
		Sommet s = sommets.get(sommet);
		if (s == null) {
			s = new Sommet(sommet);
			sommets.put(sommet, s);
		}
		Sommet s2;
		for (int i : voisin) {
			s2 = sommets.get(i);
			if (s2 == null) {
				s2 = new Sommet(i);
				sommets.put(i, s2);
			}
			s.ajouterVoisins(s2);
		}
	}


	public boolean has_frag(Graphe h) {
		for (Sommet sommet_x : sommets.values()) {
			for (Sommet sommet_y : sommet_x.getVoisins()) {
				if (h.have_edge(sommet_x, sommet_y) || h.have_edge(sommet_y, sommet_x)) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean have_edge(Sommet x, Sommet y) {
		Sommet ox = sommets.get(x.getNum_sommet());
		if (ox != null) {
			return ox.have_voisin(y);
		} else {
			return false;
		}
	}

	public boolean have_sommet(Sommet x) {
		if (sommets.get(x.getNum_sommet()) != null) {
			return true;
		} else {
			return false;
		}
	}

	public void ajouterchemin(List<Sommet> chemin) {
		int n = chemin.size();
		Sommet x, y;
		for (int i = 0; i < n - 1; i++) {
			x = chemin.get(i);
			y = chemin.get(i + 1);
			ajouterVoisins(x.getNum_sommet(), y.getNum_sommet());
			ajouterVoisins(y.getNum_sommet(), x.getNum_sommet());
		}

	}

}