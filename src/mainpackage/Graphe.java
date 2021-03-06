package mainpackage;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graphe {

	protected int nb_sommets;
	protected Map<Integer, Sommet> sommets;
	private List<Sommet> cycle;

	public Graphe(int n) {
		nb_sommets = n;
		sommets = new HashMap<Integer, Sommet>();
	}

	public Graphe(int n, Map<Integer, Sommet> g) {
		nb_sommets = n;
		sommets = g;
	}

	public Graphe() {
		sommets = new HashMap<Integer, Sommet>();
	}

	public int getNb_sommets() {
		return nb_sommets;
	}

	public List<Sommet> getSommets() {
		return new ArrayList<Sommet>(sommets.values());
	}

	public void setNb_sommets(int nb_sommets) {
		this.nb_sommets = nb_sommets;
	}

	public void setSommets(Map<Integer, Sommet> sommets) {
		this.sommets = sommets;
	}

	public void setCycle(List<Sommet> cycle) {
		this.cycle = cycle;
	}

	public Sommet getPremierSommet() {
		int firstKey = (Integer) sommets.keySet().toArray()[0];
		Sommet s = sommets.get(firstKey);
		return s;
	}

	@Override
	public String toString() {
		String s = "";
		// O(n)
		for (Sommet so : sommets.values()) {
			s += so + System.getProperty("line.separator");
		}
		return s;
	}

	// O(n+m)
	public void parcours_largeur(int dep) {
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
				if (sommet.getEtat() == Etat.Non_Atteint) {
					sommet.setEtat(Etat.Atteint);
					sommet.setDistance(peek.getDistance() + 1);
					sommet.setPere(peek);
					q.add(sommet);
				}
			}
			q.poll();
			peek.setEtat(Etat.Traite);
		}
	}

	private void cleanProperties() {
		// O(n)
		sommets.values().forEach(x -> x.cleanProperties());
	}

	// O(n+m)
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

	public Graphe createH() {
		int cyclesize = cycle.size();
		Graphe h = new Graphe(cyclesize);
		int prec, here;
		// O(n)
		for (int i = 1; i < cyclesize; i++) {
			prec = cycle.get(i - 1).getNum_sommet();
			here = cycle.get(i).getNum_sommet();
			h.ajouterVoisins(prec, here);
			h.ajouterVoisins(here, prec);
		}
		return h;
	}

	public boolean calculCycle(Sommet u, Graphe h) {
		cycle = new ArrayList<Sommet>();
		cleanProperties();
		boolean b = calculCycleRec(u);
		return b;
	}

	private boolean calculCycleRec(Sommet u) {
		u.setEtat(Etat.Atteint);
		cycle.add(u);
		List<Sommet> voisins = new ArrayList<Sommet>();
		voisins = u.getVoisins();
		for (Sommet v : voisins) {
			if (v != u.getPere()) {
				if (v.getEtat() == Etat.Atteint) {
					cycle.add(v);
					// à ce stade, le même sommet doit être présent
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

	// O(m)
	public boolean ajouterVoisins(int sommet, int... voisin) {
		Sommet s = sommets.get(sommet);
		if (s == null) {
			s = new Sommet(sommet);
			sommets.put(sommet, s);
		}
		Sommet s2;
		// O(m)
		for (int i : voisin) {
			s2 = sommets.get(i);
			if (s2 == null) {
				s2 = new Sommet(i);
				sommets.put(i, s2);
			}
			s.ajouterVoisins(s2);
		}
		return true;
	}

	public boolean addSommet(int n) {
		Sommet s = sommets.get(n);
		if (s == null) {
			sommets.put(n, new Sommet(n));
			return true;
		} else
			return false;
	}

	public boolean has_frag(Graphe h) {
		boolean cpt = false;
		// O(m)
		for (Sommet sommet_x : sommets.values()) {
			for (Sommet sommet_y : sommet_x.getVoisins()) {
				if (!(h.have_edge(sommet_x, sommet_y) || h.have_edge(sommet_y, sommet_x))) {
					cpt = true;
				}
			}
		}
		return cpt;
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
		return sommets.get(x.getNum_sommet()) != null;
	}

	// O(n)
	public void ajouterchemin(List<Sommet> chemin) {
		int n = chemin.size();
		Sommet x, y;
		// O(n)
		for (int i = 0; i < n - 1; i++) {
			x = chemin.get(i);
			y = chemin.get(i + 1);
			// O(1) car un seul voisin
			ajouterVoisins(x.getNum_sommet(), y.getNum_sommet());
			ajouterVoisins(y.getNum_sommet(), x.getNum_sommet());
		}

	}

	// O(n+m)
	public List<List<Sommet>> calcul_comp_connexe() {
		cleanProperties();
		Deque<Sommet> stack = new ArrayDeque<Sommet>();
		Deque<Sommet> non_traite = new ArrayDeque<Sommet>(sommets.values());
		List<List<Sommet>> comp = new ArrayList<List<Sommet>>();
		List<Sommet> current_comp;
		Sommet dep, peek;
		// O(n+m)
		while (!non_traite.isEmpty()) {
			dep = non_traite.pop();
			if (!dep.have_voisins()) {
				continue;
			}
			current_comp = new ArrayList<Sommet>();
			comp.add(current_comp);
			dep.setEtat(Etat.Traite);
			stack.add(dep);
			// Parcours largeur
			while (!stack.isEmpty()) {
				peek = stack.peek();
				current_comp.add(peek);
				for (Sommet sommet : peek.getVoisins()) {
					if (sommet.getEtat() == Etat.Non_Atteint) {
						sommet.setEtat(Etat.Atteint);
						stack.add(sommet);
					}
				}
				stack.poll();
				peek.setEtat(Etat.Traite);
				non_traite.remove(peek);
			}
		}
		return comp;
	}

	public Graphe diff(Graphe h) {
		Graphe inter = new Graphe(nb_sommets);
		// O(n+m)
		// Création du graphe This/H
		for (Sommet sommet : sommets.values()) {
			if (h.have_sommet(sommet))
				inter.ajouterVoisins(sommet.getNum_sommet());
			else {
				// O(m)
				for (Sommet voisins : sommet.getVoisins()) {
					inter.ajouterVoisins(sommet.getNum_sommet(), voisins.getNum_sommet());
				}
			}
		}
		return inter;
	}
}