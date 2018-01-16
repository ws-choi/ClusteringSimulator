package index.rtree.dimitris;
////////////////////////////////////////////////////////////////////////
// RTree
////////////////////////////////////////////////////////////////////////
import index.basic_ds.LinList;
import index.basic_ds.SortedLinList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
/**
* R*-tree class

* the header of the RTree is organised as follows:
* +-----------+-------------+---------------+---------------+--------------+------+
* | dimension | num_of_data | num_of_dnodes | num_of_inodes | root_is_data | root |
* +-----------+-------------+---------------+---------------+--------------+------+

*/
public final class RTree
{
    int root;                     // block # of root node
    public RTNode root_ptr;              // root-node
    public boolean root_is_data;         // true, if root is a data page
    int dimension;                // dimension of the data's

    int num_of_data;	          // # of stored data
    int num_of_dnodes;	          // # of stored data pages
    int num_of_inodes;	          // # of stored directory pages

    boolean re_level[];           // if re_level[i] is true,
                                  // there was a reinsert on level i
    LinList re_data_cands = new LinList(); // data entries to reinsert -> see insert()

    CachedBlockFile file;	      // storage manager for harddisc blocks

    int akt;                      // # of actually got data (get_next)
    byte header[];
    float node_weight[];          // weight for simulation of cache
    //protected int user_header;
    public int get_num()                 // returns # of stored data
        { return num_of_data; }

    int page_access = 0;
    
    /*
    #ifdef S3
        bool erase(PolygonId &i)             // erases object # i
        { error("RTree::erase: not implemented", false); }
    #endif
    */
    /*    Data *get_first()                    // trace
        { akt = 0; return get(akt); }        // through
        Data *get_next()                     // all
        { akt++; return get(akt); }          // data

        Data *get(int i);                    // get i-th data

    */
    /*
    #ifdef S3
        void neighbours(LinList<Data> *sl,   // berechnet fuer alle Datas in
                float eps,           // sl die Nachbarn, die in der eps-
                Result *rs,          // Umgebung liegen
                norm_ptr norm);
    #endif // S3
    */

    /**
    * Construct a new R*-tree
    */
    public RTree(String fname, int _b_length, int cache_size, int _dimension)
    // neuen R-Baum konstruieren
    {
        node_weight = new float[20];

        try
        {
            file = new CachedBlockFile(fname, _b_length, cache_size);
        }
        catch (IOException e)
        {
            Constants.error("RTree creation: error in block file initialization", true);
        }

        // header allokieren und lesen
        header = new byte [file.get_blocklength()];

        // das ist zwar an sich hier Quatsch, aber damit wird die Variable
        // user_header richtig gesetzt, so dass beim Schreiben des Headers der
        // Aufrufer kein problem hat
        try
        {
            read_header(header);
        }
        catch (Exception e){}

        dimension = _dimension;
        root = 0;
        root_ptr = null;
        root_is_data = true;
        num_of_data = num_of_inodes = num_of_dnodes = 0;

        root_ptr = new RTDataNode(this);
        root = root_ptr.block;
    }

    /**
    * Construct(read) an R*-tree from disk
    */
    RTree(String fname, int cache_size)
    // bereits vorhandenen R-Baum laden
    {
        node_weight = new float[20];

        // file existiert schon --> Blockgroesse wird im Konstruktor ignoriert
        try
        {
            file = new CachedBlockFile(fname, 0, cache_size);
        }
        catch (IOException e)
        {
            Constants.error("RTree reading: error in block file initialization", true);
        }

        // header allokieren und lesen
        header = new byte [file.get_blocklength()];
        try
        {
            file.read_header(header);
        }
        catch (IOException e)
        {
            Constants.error("RTree header reading: error in block file initialization", true);
        }

        try
        {
            read_header(header);
        }
        catch (Exception e) {}

        root_ptr = null;

        if (get_num() == 0)
        // Baum war leer -> Datenknoten anlegen und d einfuegen
        {
                root_ptr = new RTDataNode(this);
                root = root_ptr.block;
                root_ptr.level = 0;
        }
        else
                load_root();
    }

    /*
    void printinfo(FILE *f)
    {
        fprintf(f,"\n internal: %d data-nodes : %d Datas: %d \n",
        num_of_inodes, num_of_dnodes, num_of_data);
    }*/

    /**
    * read serialized information from buffer to rtree variables
    */
    void read_header(byte buffer[]) throws IOException
    {
        ByteArrayInputStream byte_in = new ByteArrayInputStream(buffer);
        DataInputStream in = new DataInputStream(byte_in);

        dimension = in.readInt();
        num_of_data = in.readInt();
        num_of_dnodes = in.readInt();
        num_of_inodes = in.readInt();
        root_is_data = in.readBoolean();
        root = in.readInt();

        //user_header = &buffer[i];

        in.close();
        byte_in.close();
    }

    /**
    * store rtree information to buffer serially
    */
    void write_header(byte buffer[]) throws IOException
    {
        ByteArrayOutputStream byte_out = new ByteArrayOutputStream(buffer.length);
        DataOutputStream out = new DataOutputStream(byte_out);

        out.writeInt(dimension);
        out.writeInt(num_of_data);
        out.writeInt(num_of_dnodes);
        out.writeInt(num_of_inodes);
        out.writeBoolean(root_is_data);
        out.writeInt(root);
        out.writeInt(dimension);

        //user_header = &buffer[i];
        
        byte[] bytes = byte_out.toByteArray();
        for (int i = 0; i < bytes.length; ++i)
                buffer[i] = bytes[i];

        out.close();
        byte_out.close();
    }

    /**
    * load root information from disk
    */
    public void load_root()
    {
        if (root_ptr == null)
        // create root and read its info from the appropriate disk block (root)
        // See RTDataNode and RTDirNode constructor
        if (root_is_data)
            root_ptr = new RTDataNode(this, root);
        else
            root_ptr = new RTDirNode(this, root);
    }

    /**
    * Insert a new data entry into the tree. Insertion is propagated to the
    * root node. If the root overflows, it has to be split into 2 nodes and
    * a new root will be introduced, to hold the split nodes.
    */
    public void insert(Data d)
    {
        int i, j;                                            // counters
        RTNode sn[] = new RTNode[1];            // potential new node when SPLIT takes place
        RTDirNode nroot_ptr;                    // new root when the root is SPLIT
        int nroot;                                        // block # of nroot_ptr
        DirEntry de;                                    // temp Object used to consruct new
                                                                    // root dir entries when SPLIT takes place
        int split_root = Constants.NONE;  // return of root_ptr.insert(d)
        Data d_cand, dc;                            // temp duplicates of d
        float nmbr[];                                    // root_ptr MBR

        // load root into memory
        load_root();

        /*
        * no overflow occured until now. re_level array indicates if a re_insert
        * has been done at the specific level of the tree. Initially all entries
        * in this array should be set to false
        */
        re_level = new boolean[root_ptr.level+1];
        for (i = 0; i <= root_ptr.level; i++)
            re_level[i] = false;

        /*
        * insert d into re_data_cands as the first entry to insert
        * make a copy of d because it shouldnt be erased later
        */
        dc = (Data)d.clone(); //duplicate the data into dc
        re_data_cands.insert(dc); //insert the datacopy into the
                                 //list of pending to be inserted data
        j = -1;
        while (re_data_cands.get_num() > 0)
        {
            // first try to insert data, then directory entries
            d_cand = (Data)re_data_cands.get_first();
            if (d_cand != null)
            {
                // since erase deletes the according data element of the
                // list, we should make a copy of the data before
                // erasing it
                dc = (Data)d_cand.clone();
                re_data_cands.erase();

                // start recursive insert from root
                split_root = ((Node)root_ptr).insert(dc, sn);
            }
            else
                Constants.error("RTree::insert: inconsistent list re_data_cands", true);

            if (split_root == Constants.SPLIT)
            /*
            * insert has lead to split --> create new root having as sons
            * old root and sn
            */
            {
                //initialize new root
                nroot_ptr = new RTDirNode(this);
                nroot_ptr.son_is_data = root_is_data;
                nroot_ptr.level = (short)(root_ptr.level + 1);
                nroot = nroot_ptr.block;

                // a new direntry is introduced having as son the old root
                de = new DirEntry(dimension, root_is_data, this);
                nmbr = ((Node)root_ptr).get_mbr();
                // store the mbr of the root to the direntry
                System.arraycopy(nmbr, 0, de.bounces, 0, 2*dimension);
                de.son = root_ptr.block;
                de.son_ptr = root_ptr;
                de.son_is_data = root_is_data;
                de.num_of_data = ((Node)root_ptr).get_num_of_data();
                // add de to the new root
                nroot_ptr.enter(de);

                // a new direntry is introduced having as son the brother(split) of the old root
                de = new DirEntry(dimension, root_is_data, this);
                nmbr = ((Node)sn[0]).get_mbr();
                System.arraycopy(nmbr, 0, de.bounces, 0, 2*dimension);
                de.son = sn[0].block;
                de.son_ptr = sn[0];
                de.son_is_data = root_is_data;
                de.num_of_data = ((Node)sn[0]).get_num_of_data();
                nroot_ptr.enter(de);

                // replace the root of the tree with the new node
                root = nroot;
                root_ptr = nroot_ptr;

                //System.out.println("New root direntries:");
                //for (int l = 0; l < root_ptr.get_num(); l++)
                //{
                //    for (int k=0; k<2*dimension; k++)
                //        System.out.print(((RTDirNode)root_ptr).entries[l].bounces[k] + " ");
                //    System.out.println(" ");
                //}

              // the new root is a directory node
              root_is_data = false;
            }
            // go to the next data object to be (re)inserted
            j++;
        }

        // increase number of data in the tree after insertion
        num_of_data++;
    }

    /**
    * Return the ith data element in the tree.
    * --> Propagate to the root node.
    */
    Data get(int i)
    {
        Data d;

        // load root into main memory
        load_root();

        // propagate to the root node
        d = ((Node)root_ptr).get(i);

        return d;
    }

    /**
    * Print the objects in the tree that intersect with the parameter mbr.
    * --> Propagate to the root node.
    */
    void region(float mbr[])
    {
        // load root node into main memory
        load_root();

        ((Node)root_ptr).region(mbr);
    }

    /**
    * Print the objects in the tree that intersect with the parameter point.
    * --> Propagate to the root node.
    */
    void point_query(float p[])
    {
        // load root node into main memory
        load_root();

        ((Node)root_ptr).point_query(p);
    }


    /*
    #ifdef S3
    template <class Data>
    void RTree<Data>::neighbours(LinList<Data> *sl,
                float eps,
                Result *rs,
                norm_ptr norm)
    {
        // load root node into main memory
        load_root();

        root_ptr->neighbours(sl, eps, rs, norm);
    }
    #endif // S3
    */

    /**
    * Return to Nearest the object in the tree that is nearest
    * to parameter point. --> Propagate to the root node.
    */
    void NearestNeighborQuery(PPoint QueryPoint, PPoint Nearest)
    {
          float/*[]*/ nearest_distanz/* = new float[1]*/;

          // load root node into main memory
          load_root();

          nearest_distanz/*[0]*/ = Constants.MAXREAL;

          ((Node)root_ptr).NearestNeighborSearch(QueryPoint, Nearest, nearest_distanz);
    }

    /**
    * Return to res the objects in the tree that are nearest
    * to parameter point. --> Propagate to the root node.
    */
    void NearestNeighborQuery(PPoint QueryPoint, SortedLinList res)
    {
          float nearest_distanz;

          // load root node into main memory
          load_root();

          nearest_distanz = Constants.MAXREAL;

          ((Node)root_ptr).NearestNeighborSearch(QueryPoint, res, nearest_distanz);
    }

    /**
    * Return to res the k objects in the tree that are nearest
    * to parameter point. --> Propagate to the root node.
    */
    void k_NearestNeighborQuery(PPoint QueryPoint, int k, SortedLinList NeighborList)
    {
        int i,l,n;
        float nearest_distanz;
        Data p;

        nearest_distanz = Constants.MAXREAL;

        load_root();

        // NeighborListListe vorbereiten
        NeighborList.set_sorting(true);

        for(i = 0; i < k; i++)
        {
                p = new Data(dimension);
                for(l = 0; l < dimension; l++)
                    p.data[l] = Constants.MAXREAL;
                p.distanz = Constants.MAXREAL;
                NeighborList.insert(p);
        }

        ((Node)root_ptr).NearestNeighborSearch(QueryPoint,NeighborList,nearest_distanz);
    }
    
    /**
    * Return to res the objects in the tree that intersect with the parameter point.
    * --> Propagate to the root node.
    */
    void point_query(PPoint p, SortedLinList res)
    {
        page_access = 0;
        
        // load root node into main memory
        load_root();
        ((Node)root_ptr).point_query(p, res);
    }

    /**
    * Return to res the objects in the tree that intersect with the parameter circle.
    * --> Propagate to the root node.
    */
    public void rangeQuery(PPoint center, float radius, SortedLinList res)
    {
        page_access = 0;
        
        load_root();
        ((Node)root_ptr).rangeQuery(center,radius,res);
    }
    
    /**
    * Return to res the objects in the tree that intersect with the parameter ring,
    * defined by the circles provided that radius1<radius2.
    * --> Propagate to the root node.
    */
    void ringQuery(PPoint center, float radius1, float radius2, SortedLinList res)
    {
        page_access = 0;

        load_root();
        ((Node)root_ptr).ringQuery(center,radius1,radius2,res);
    }

    /**
    * Return to res the objects in the tree that intersect with the parameter mbr.
    * --> Propagate to the root node.
    */
    void rangeQuery(float mbr[], SortedLinList res)
    {
        page_access = 0;

        load_root();
        ((Node)root_ptr).rangeQuery(mbr,res);
    }


    void range_nnQuery(float mbr[], SortedLinList res, PPoint Nearest)
    {
        PPoint center;
        float distanz = Constants.MAXREAL;
        boolean success = false;
        int i;

        center = new PPoint(dimension);
        for(i = 0; i < dimension; i++)
                center.data[i] = (mbr[2*i] + mbr[2*i+1]) / (float)2.0;

        load_root();

        ((Node)root_ptr).range_nnQuery(mbr,res,center, distanz, Nearest, success);
    }


    void overlapping(float p[], int nodes_t[])
    {
        load_root();

        ((Node)root_ptr).overlapping(p, nodes_t);
    }


    /**
    * Return to nodes_a[] the # of nodes at each level of the tree.
    * --> Propagate to the root node.
    */
    void nodes(int nodes_a[])
    {
        load_root();

        ((Node)root_ptr).nodes(nodes_a);
    }

    /*
    void RTree<Data>::writeinfo(FILE *f)
    {
        load_root();
        root_ptr->writeinfo(f);
        fprintf(f,"\n");
    }*/

    /**
    * Store to node_weight[] how many of the directory pages starting from the root
    * level can be stored in a cache with the paremeter cache size. Each entry in
    * node_weight stores the fraction of nodes at the specific level that will not
    * fit in the cache. For instance, node_weight[root_ptr.level=10]=0,
    * node_weight[9]=0, node_weight[8]=0, node_weight[7]=0.4, node_weight[6]=1,...
    * means that only the entries of the upper 3 levels can fully fit in cache, and
    * also a portion of the nodes at the 7th level.
    */
    void set_node_weight(int cache_size)
    {
        int nodes_tab[] = new int[20]; // holds the # of nodes at each level 0..19
        int i, j, rest;
        boolean c = false;

        // calculate number of nodes per level
        for (i = 0; i < 20; i++)
                nodes_tab[i] = 0;

        nodes(nodes_tab); // propagate to root

        rest = cache_size;
        if (cache_size > 0)
                c = true;

        for (i = root_ptr.level; i > 0; i--)
        {
            if (c)
            // cache is not fully filled
            {
                if (rest - nodes_tab[i] > 0)
                // there is enough cache for this level
                {
                    rest -= nodes_tab[i];
                    node_weight[i] = (float)0.0;
                }
                else
                {
                    // this is the last level to be cached and not all
                    // nodes of the level are in general in the cache
                    node_weight[i] = (float)1.0 - ((float) rest / (float)nodes_tab[i]);
                    rest = 0;
                    c = false;
                }
            }
            else
                // cache is full
            node_weight[i] = (float)1.0;
        }

        // data nodes are not in cache
        node_weight[0] = (float)1.0;

        System.out.println("cache configuration (" + cache_size + " blocks cache)");
            for (i = 0; i < 10; i++)
                System.out.println("level " + i + ": " + node_weight[i]+ " of " + nodes_tab[i]);
    }
    
    protected void delete ()
    {
        try
        {
            write_header(header);
            file.set_header(header);
        }
        catch (IOException e)
        {
            Constants.error("RTree.delete: error in writing header", true);
        }
        if (root_ptr != null)
        {
            ((Node)root_ptr).delete();
        }
        try
        {
            file.flush();
        }
        catch (IOException e)
        {
            Constants.error("RTree.delete: error in flushing file", true);
        }
       
        System.out.println("Rtree saved: num_of_data=" + num_of_data +
                                       " num_of_inodes=" + num_of_inodes +
                                       " num_of_dnodes=" + num_of_dnodes); 
    }
    
    /*
    protected void finalize () throws Throwable
    {
        write_header(header);
        System.out.println("Rtree saved: num_of_data=" + num_of_data +
                                       " num_of_inodes=" + num_of_inodes +
                                       " num_of_dnodes=" + num_of_dnodes);
        
    }*/
    
    // converts a rect_to_rect relation to an intermediateMBR_to_rect relation
    // see SIGMOD'95 paper for details
    public static short toMBRtopo(short topology)
    {
    	short ret = topology;
    	
    	if (relationSet.getBit(topology,(byte)0) == 1) // disjoint
    		ret |= relationSet.p2((byte)1) + relationSet.p2((byte)7) + relationSet.p2((byte)6)
    					 + relationSet.p2((byte)5);
    	
    	if (relationSet.getBit(topology,(byte)1) == 1) // meet
    		ret |= relationSet.p2((byte)7) + relationSet.p2((byte)6) + relationSet.p2((byte)5);
    		
		if (relationSet.getBit(topology,(byte)2) == 1) // equal
    		ret |= relationSet.p2((byte)6) + relationSet.p2((byte)5);
    		
    	if (relationSet.getBit(topology,(byte)3) == 1) // inside
    		ret |= relationSet.p2((byte)7) + relationSet.p2((byte)4) + relationSet.p2((byte)2)
    				+ relationSet.p2((byte)6) + relationSet.p2((byte)5);

    	if (relationSet.getBit(topology,(byte)4) == 1) // covered_by
    		ret |= relationSet.p2((byte)7) + relationSet.p2((byte)2)
    				+ relationSet.p2((byte)6) + relationSet.p2((byte)5);
		
		// skip contain
		
		if (relationSet.getBit(topology,(byte)6) == (byte)1) // covers
    		ret |= relationSet.p2((byte)5);
    	
    	if (relationSet.getBit(topology,(byte)7) == (byte)1) // overlap
    		ret |= relationSet.p2((byte)6) + relationSet.p2((byte)5);
    	
    	return ret;
    }
    
    void constraints_query(rectangle rect, double distance[], short direction, short topology, SortedLinList res)
    {
    	short MBRtopo = toMBRtopo(topology);
    	
    	page_access = 0;
        
        load_root();
        ((Node)root_ptr).constraints_query(rect, distance, direction, MBRtopo, topology, res);
    }
    
    public static void main(String[] args) {
		RTree rtree = new RTree("for_Skyline", 128, 0, 2);
		Data newdata;
		int cnt= 1;
		
		//1
		newdata = new Data(2, cnt++);
		newdata.data[0] = 1;	newdata.data[1] = 1;	newdata.data[2] = 9;	newdata.data[3] = 9;
		rtree.insert(newdata);
		
		//2
		newdata = new Data(2, cnt++);
		newdata.data[0] = 2;	newdata.data[1] = 2;	newdata.data[2] = 10;	newdata.data[3] = 10;
		rtree.insert(newdata);		
		
		//3
		newdata = new Data(2, cnt++);
		newdata.data[0] = 4;	newdata.data[1] = 4;	newdata.data[2] = 8;	newdata.data[3] = 8;
		rtree.insert(newdata);
		
		//4
		newdata = new Data(2, cnt++);
		newdata.data[0] = 6;	newdata.data[1] = 6;	newdata.data[2] = 7;	newdata.data[3] = 7;
		rtree.insert(newdata);
		
		//5
		newdata = new Data(2, cnt++);
		newdata.data[0] = 9;	newdata.data[1] = 9;	newdata.data[2] = 10;	newdata.data[3] = 10;
		rtree.insert(newdata);
		
		//6
		newdata = new Data(2, cnt++);
		newdata.data[0] = 7;	newdata.data[1] = 7;	newdata.data[2] = 5;	newdata.data[3] = 5;
		rtree.insert(newdata);
		
		//7
		newdata = new Data(2, cnt++);
		newdata.data[0] = 5;	newdata.data[1] = 5;	newdata.data[2] = 6;	newdata.data[3] = 6;
		rtree.insert(newdata);
		
		
		//8
		newdata = new Data(2, cnt++);
		newdata.data[0] =4;	newdata.data[1] = 4;	newdata.data[2] = 3;	newdata.data[3] = 3;
		rtree.insert(newdata);
		
		
		//9
		newdata = new Data(2, cnt++);
		newdata.data[0] = 3;	newdata.data[1] = 3;	newdata.data[2] = 2;	newdata.data[3] = 2;
		rtree.insert(newdata);
		
		
		//10
		newdata = new Data(2, cnt++);
		newdata.data[0] =9 ;	newdata.data[1] =9 ;	newdata.data[2] = 1;	newdata.data[3] =1 ;
		rtree.insert(newdata);
		
		
		//11
		newdata = new Data(2, cnt++);
		newdata.data[0] = 10;	newdata.data[1] = 10;	newdata.data[2] = 4;	newdata.data[3] = 4;
		rtree.insert(newdata);
		
		
		//12
		newdata = new Data(2, cnt++);
		newdata.data[0] = 6;	newdata.data[1] =6 ;	newdata.data[2] = 2;	newdata.data[3] =2 ;
		rtree.insert(newdata);
		
		
		//13
		newdata = new Data(2, cnt++);
		newdata.data[0] = 8;	newdata.data[1] = 8;	newdata.data[2] =3 ;	newdata.data[3] =3 ;
		rtree.insert(newdata);
		
		
		
		
		
		rtree.skyline();
	}
    
	public SortedLinList skyline() {
		
		load_root();
	
		SortedLinList Result_Set = new SortedLinList();
		SortedLinList heap = new SortedLinList();
		
		if(root_is_data)
		{
			Constants.error("RTree,  skline", true);
		}
		
		else{
			
			RTDirNode node = (RTDirNode) root_ptr;
			
			DirEntry[] entries = node.entries;
			
			for (int i = 0; i < node.num_entries; i++) {
				DirEntry entry = entries[i];
				heap.insert(new Dir_Entry(entry.get_son()));
			}
		}
				
		heap.sort();
		
		while(heap.get_num() > 0)
		{
			HeapEntry obj = (HeapEntry) heap.get_first(); heap.erase();
						
			if( isDominated(obj.mbr, Result_Set)) continue;
			
			else{
				
				if(obj instanceof Data_Entry){
					Data_Entry data_entry = (Data_Entry) obj;					
					data_entry.data.distanz = data_entry.dist;
					Result_Set.insert(obj);
				}
				
				else{
					
					Dir_Entry entry = (Dir_Entry) obj;
					if(entry.isDataNode)
					{
						RTDataNode node = (RTDataNode)entry.node;
						
						Data[] data = node.data;
						for (int i = 0; i < node.num_entries; i++) {
							Data child = data[i];
							
							if(isDominated(child.data, Result_Set) ) continue;
							
							else heap.insert(new Data_Entry(child));
						}
							
					}
					
					else{
						
						RTDirNode node = (RTDirNode)entry.node;
						
						DirEntry[] entries = node.entries;
						for (int i = 0; i < node.num_entries; i++) {
							
							DirEntry child = entries[i];
							
							if(child.son_is_data)
							{
								RTDataNode child_ptr = (RTDataNode) child.get_son();
								
								if( isDominated(child_ptr.get_mbr(), Result_Set) ) continue;
								
								else
									heap.insert(new Dir_Entry(child_ptr));
							}
							
							else{
								
								RTDirNode child_ptr = (RTDirNode) child.get_son();
								
								if( isDominated(child_ptr.get_mbr(), Result_Set) ) continue;
								
								else
									heap.insert(new Dir_Entry(child_ptr));
								
							}
						}
						
					}
					
				heap.sort();	
				}
			}
			
			
			
		}
		
		Result_Set.sort();
		
		SortedLinList result = new SortedLinList();
		
		while(Result_Set.get_num() > 0){
//			System.out.println(((Data_Entry)Result_Set.get_first()).data);
			result.insert(((Data_Entry)Result_Set.get_first()).data);
			Result_Set.erase();
		}
	
		return result;
	}
	
	 public void my_insert(Data d) //dont clone!
	    {
	        int i, j;                                            // counters
	        RTNode sn[] = new RTNode[1];            // potential new node when SPLIT takes place
	        RTDirNode nroot_ptr;                    // new root when the root is SPLIT
	        int nroot;                                        // block # of nroot_ptr
	        DirEntry de;                                    // temp Object used to consruct new
	                                                                    // root dir entries when SPLIT takes place
	        int split_root = Constants.NONE;  // return of root_ptr.insert(d)
	        Data d_cand, dc;                            // temp duplicates of d
	        float nmbr[];                                    // root_ptr MBR

	        // load root into memory
	        load_root();

	        /*
	        * no overflow occured until now. re_level array indicates if a re_insert
	        * has been done at the specific level of the tree. Initially all entries
	        * in this array should be set to false
	        */
	        re_level = new boolean[root_ptr.level+1];
	        for (i = 0; i <= root_ptr.level; i++)
	            re_level[i] = false;

	        /*
	        * insert d into re_data_cands as the first entry to insert
	        * make a copy of d because it shouldnt be erased later
	        */
	        dc = d; //duplicate the data into dc
	        re_data_cands.insert(dc); //insert the datacopy into the
	                                 //list of pending to be inserted data
	        j = -1;
	        while (re_data_cands.get_num() > 0)
	        {
	            // first try to insert data, then directory entries
	            d_cand = (Data)re_data_cands.get_first();
	            if (d_cand != null)
	            {
	                // since erase deletes the according data element of the
	                // list, we should make a copy of the data before
	                // erasing it
	                dc = d;
	                re_data_cands.erase();

	                // start recursive insert from root
	                split_root = ((Node)root_ptr).insert(dc, sn);
	            }
	            else
	                Constants.error("RTree::insert: inconsistent list re_data_cands", true);

	            if (split_root == Constants.SPLIT)
	            /*
	            * insert has lead to split --> create new root having as sons
	            * old root and sn
	            */
	            {
	                //initialize new root
	                nroot_ptr = new RTDirNode(this);
	                nroot_ptr.son_is_data = root_is_data;
	                nroot_ptr.level = (short)(root_ptr.level + 1);
	                nroot = nroot_ptr.block;

	                // a new direntry is introduced having as son the old root
	                de = new DirEntry(dimension, root_is_data, this);
	                nmbr = ((Node)root_ptr).get_mbr();
	                // store the mbr of the root to the direntry
	                System.arraycopy(nmbr, 0, de.bounces, 0, 2*dimension);
	                de.son = root_ptr.block;
	                de.son_ptr = root_ptr;
	                de.son_is_data = root_is_data;
	                de.num_of_data = ((Node)root_ptr).get_num_of_data();
	                // add de to the new root
	                nroot_ptr.enter(de);

	                // a new direntry is introduced having as son the brother(split) of the old root
	                de = new DirEntry(dimension, root_is_data, this);
	                nmbr = ((Node)sn[0]).get_mbr();
	                System.arraycopy(nmbr, 0, de.bounces, 0, 2*dimension);
	                de.son = sn[0].block;
	                de.son_ptr = sn[0];
	                de.son_is_data = root_is_data;
	                de.num_of_data = ((Node)sn[0]).get_num_of_data();
	                nroot_ptr.enter(de);

	                // replace the root of the tree with the new node
	                root = nroot;
	                root_ptr = nroot_ptr;

	                //System.out.println("New root direntries:");
	                //for (int l = 0; l < root_ptr.get_num(); l++)
	                //{
	                //    for (int k=0; k<2*dimension; k++)
	                //        System.out.print(((RTDirNode)root_ptr).entries[l].bounces[k] + " ");
	                //    System.out.println(" ");
	                //}

	              // the new root is a directory node
	              root_is_data = false;
	            }
	            // go to the next data object to be (re)inserted
	            j++;
	        }

	        // increase number of data in the tree after insertion
	        num_of_data++;
	    }
	private boolean isDominated (float[] mbr, SortedLinList list){
		
		boolean result = false;

		for(int i=0; i<list.get_num(); i++)
		{
			boolean dominated = true;

			HeapEntry skyline_obj = (HeapEntry) list.get(i);
			for(int j =0 ; j<dimension; j++)				
				if(skyline_obj.mbr[2*j+1] > mbr[2*j])
					dominated = false;
			
			if(dominated) return true;

		}
		
		return result;
	}
	
}

class HeapEntry implements Comparable{

	
	float dist;
	public float[] mbr;

	public void setDist(float dist) {
		this.dist = dist;
	}
	
	public float getDist() {
		return dist;
	}
	
	@Override
	public int compare(Object other) {
		
		if ( !(other instanceof HeapEntry) ) return -1;
		
		HeapEntry you = (HeapEntry) other;
		
		float mydist = dist;
		float yourdist = you.dist;
		
		if (mydist > yourdist) return -1;
		else if (mydist < yourdist) return +1;
		else return 0;
	}
	
}

class Dir_Entry extends HeapEntry
{

	RTNode node;
	boolean isDataNode;
	
	public Dir_Entry( RTNode node ) {
		
		if( !(node instanceof RTNode) )
			Constants.error("not node.", true);
		
		this.node = node;
		
		if( node instanceof RTDataNode )
		{
			isDataNode = true;
			RTDataNode thisnode = (RTDataNode) node;
			
			dist = 0;
			
			for(int i = 0 ; i< thisnode.dimension; i++)
				dist += thisnode.get_mbr()[i*2];	
			
			mbr = thisnode.get_mbr().clone();
		}
		
		else if ( node instanceof RTDirNode)
		{
			isDataNode = false;
			RTDirNode thisnode = (RTDirNode) node;
			
			dist = 0;
			
			for(int i = 0 ; i< thisnode.dimension; i++)
				dist += thisnode.get_mbr()[i*2];	
			

			mbr = thisnode.get_mbr().clone();
		}
		
		else
			Constants.error("not node.", true);
	}
	
}

class Data_Entry extends HeapEntry
{

	Data data;
	
	public Data_Entry( Data data ) {
		
		if( !(data instanceof Data) )
			Constants.error("not data.", true);
		
		this.data = data;
		
		for(int i = 0 ; i< data.dimension; i++)
			dist += data.get_mbr()[i*2];	
		
		mbr = data.data.clone();
		
	}
	
}
