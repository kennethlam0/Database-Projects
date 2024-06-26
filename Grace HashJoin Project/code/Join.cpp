#include "Join.hpp"
#include <iostream>
#include <vector>

using namespace std;

/*
 * Input: Disk, Memory, Disk page ids for left relation, Disk page ids for right relation
 * Output: Vector of Buckets of size (MEM_SIZE_IN_PAGE - 1) after partition
 */
vector<Bucket> partition(Disk* disk, Mem* mem, pair<uint, uint> left_rel,
                         pair<uint, uint> right_rel) {
	// TODO: implement partition phase

	vector<Bucket> partitions(MEM_SIZE_IN_PAGE - 1, Bucket(disk)); // placeholder

	// Reset all pages in memory each time we start a new partition
	for(uint i = 0; i < MEM_SIZE_IN_PAGE ; i++){
		mem->mem_page(i)->reset();
	}
	

	for (uint i = left_rel.first; i < left_rel.second; i++) {
		mem->loadFromDisk(disk, i, MEM_SIZE_IN_PAGE - 1);
		Page* p = mem->mem_page(MEM_SIZE_IN_PAGE - 1);
		for(uint j = 0; j < p->size(); j++){
			Record r  = p->get_record(j);
			uint hash = r.partition_hash();
			uint position = hash % (MEM_SIZE_IN_PAGE - 1);
			Page* record = mem->mem_page(position);
			if(record->full()){
				partitions[position].add_left_rel_page(mem->flushToDisk(disk, position));
			}
            mem->mem_page(position)->loadRecord(r);
		}

	}

	// write to disk all partially full pagew once the relation is exhausted LEFT RELATION
	for(uint i = 0; i < MEM_SIZE_IN_PAGE-1; i++){
		if(!mem->mem_page(i)->empty()){
			partitions[i].add_left_rel_page(mem->flushToDisk(disk, i));
		}
	}

	for (uint i = right_rel.first; i < right_rel.second; i++) {
		mem->loadFromDisk(disk, i, MEM_SIZE_IN_PAGE - 1);
		Page* p = mem->mem_page(MEM_SIZE_IN_PAGE - 1);
		for(uint j = 0; j < p->size(); j++){
			Record r  = p->get_record(j);
			uint hash = r.partition_hash();
			uint position = hash % (MEM_SIZE_IN_PAGE - 1);
			Page* record = mem->mem_page(position);
			if(record->full()){
				partitions[position].add_right_rel_page(mem->flushToDisk(disk, position));
			}
            mem->mem_page(position)->loadRecord(r);
		}

	}

	// write to disk all partially full page once the relation is exhausted RIGHT RELATION
	for(uint i = 0; i < MEM_SIZE_IN_PAGE-1; i++){
		if(!mem->mem_page(i)->empty()){
			partitions[i].add_right_rel_page(mem->flushToDisk(disk, i));
		}	
	}

	return partitions;
}

/*
 * Input: Disk, Memory, Vector of Buckets after partition
 * Output: Vector of disk page ids for join result
 */
vector<uint> probe(Disk* disk, Mem* mem, vector<Bucket>& partitions) {
    vector<uint> disk_pages;
    mem->mem_page(MEM_SIZE_IN_PAGE-1)->reset();
    
    for (Bucket bucket : partitions) {
        // WE SHOULD RESET FIRST THEN PROBE
        for (uint i = 0; i < MEM_SIZE_IN_PAGE - 2; i++) {
            mem->mem_page(i)->reset();
        }
        vector<uint> R = bucket.get_left_rel();
        vector<uint> S = bucket.get_right_rel();
        if(R.size() > S.size()){
            // swap r and s
            vector<uint> temp = R;
            R = S;
            S = temp;
        }
        for (uint pageID : R) {
            mem->loadFromDisk(disk, pageID, MEM_SIZE_IN_PAGE - 2); 
            Page* page1 = mem->mem_page(MEM_SIZE_IN_PAGE - 2);
            for (uint j = 0; j < page1->size(); j++) {
                Record record1 = page1->get_record(j);
                uint hash_position = record1.probe_hash() % (MEM_SIZE_IN_PAGE - 2);
                Page* hash_page = mem->mem_page(hash_position);
                hash_page->loadRecord(record1);
            }
            page1->reset();
        }

        for (uint pageID : S) {
            mem->loadFromDisk(disk, pageID, MEM_SIZE_IN_PAGE - 2);
            Page* page2 = mem->mem_page(MEM_SIZE_IN_PAGE - 2);
            for (uint k = 0; k < page2->size(); k++) {
                Record record2 = page2->get_record(k);
                uint hash_position2 = record2.probe_hash() % (MEM_SIZE_IN_PAGE - 2);
                Page* hash_page2 = mem->mem_page(hash_position2);
                for (uint x = 0; x < hash_page2->size(); x++) {
                    Record record3 = hash_page2->get_record(x);
                    if (record3 == record2) {
                        Page* result_page = mem->mem_page(MEM_SIZE_IN_PAGE - 1);
                        if (result_page->full()) {
                            disk_pages.push_back(mem->flushToDisk(disk, MEM_SIZE_IN_PAGE - 1));
                            result_page->reset();
                        }
                        result_page->loadPair(record3, record2); 
                    }
                }
            }
        }

    }

    // Flush any remaining results in the result page
    Page* resultPage = mem->mem_page(MEM_SIZE_IN_PAGE - 1);
    if (!resultPage->empty()) {
        disk_pages.push_back(mem->flushToDisk(disk, MEM_SIZE_IN_PAGE - 1));
        resultPage->reset(); 
    }

    return disk_pages;
}