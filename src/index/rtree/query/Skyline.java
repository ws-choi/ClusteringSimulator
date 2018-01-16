package index.rtree.query;


import index.basic_ds.SortedLinList;
import index.rtree.dimitris.Data;
import index.rtree.dimitris.DirEntry;
import index.rtree.dimitris.RTDataNode;
import index.rtree.dimitris.RTDirNode;
import index.rtree.dimitris.RTNode;
import index.rtree.dimitris.RTree;
import index.rtree.query.event.Skyline_Query_Event;
import index.rtree.query.skyline.Data_Entry;
import index.rtree.query.skyline.Dir_Entry;
import index.rtree.query.skyline.HeapEntry;
import index.rtree.query.skyline.hotel.Hotel;

import javax.swing.JPanel;

import query.Skyline_Query;

import com.skyline.global.MyConstants;

import data.Point_Float;

public class Skyline implements Skyline_Query {

	protected RTree tree;
	protected JPanel target;
	protected long sleep_time;
	protected Point_Float point;
	
	
	public Skyline(RTree tree, JPanel target, Skyline_Query_Event query, long time) {

		this.tree = tree;
		this.target = target;
		this.sleep_time = time;
		this.point = query.point;
		
	}
	
	

	public SortedLinList skyline() {
		System.out.println("me im fine");
		tree.load_root();
		
		RTNode root_ptr = tree.root_ptr;
	
		SortedLinList Result_Set = new SortedLinList();
		SortedLinList heap = new SortedLinList();
		
		if(tree.root_is_data)
		{
		}
		
		else{
			
			RTDirNode node = (RTDirNode) root_ptr;
			
			DirEntry[] entries = node.entries;
			
			for (int i = 0; i < node.get_num(); i++) {
				DirEntry entry = entries[i];
				heap.insert(new Dir_Entry(entry.get_son()));
			}
		}
				
		heap.sort();
		
		while(heap.get_num() > 0)
		{
			HeapEntry obj = (HeapEntry) heap.get_first(); heap.erase();
						
			if( MyConstants.isDominated(obj.mbr, Result_Set)) continue;
			
			else{
				
				if(obj instanceof Data_Entry){
					Data_Entry data_entry = (Data_Entry) obj;					
					data_entry.data.distanz = data_entry.dist;
					Result_Set.insert(obj);
					
					Hotel hotel = (Hotel) (data_entry.data);
					hotel.selected = true;
					
					sleep_for_a_while(sleep_time);
					target.repaint();
				}
				
				else{
					
					Dir_Entry entry = (Dir_Entry) obj;
					if(entry.isDataNode)
					{
						RTDataNode node = (RTDataNode)entry.node;
						
						Data[] data = node.data;
						for (int i = 0; i < node.get_num(); i++) {
							Data child = data[i];
							
							if(MyConstants.isDominated(child.data, Result_Set) ) continue;
							
							else heap.insert(new Data_Entry(child));
						}
							
					}
					
					else{
						
						RTDirNode node = (RTDirNode)entry.node;
						
						DirEntry[] entries = node.entries;
						for (int i = 0; i < node.get_num(); i++) {
							
							DirEntry child = entries[i];
							
							if(child.son_is_data)
							{
								RTDataNode child_ptr = (RTDataNode) child.get_son();
								
								if( MyConstants.isDominated(child_ptr.get_mbr(), Result_Set) ) continue;
								
								else
									heap.insert(new Dir_Entry(child_ptr));
							}
							
							else{
								
								RTDirNode child_ptr = (RTDirNode) child.get_son();
								
								if( MyConstants.isDominated(child_ptr.get_mbr(), Result_Set) ) continue;
								
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
			
			result.insert(((Data_Entry)Result_Set.get_first()).data);
			Result_Set.erase();
		}
	
		return result;
	}
	
	protected void sleep_for_a_while(long sleep_time2) {
		
		try {
			Thread.sleep(sleep_time2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}



	@Override
	public void run() {
		skyline();
	}
	
	
}
