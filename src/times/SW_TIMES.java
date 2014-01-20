package times;

public class SW_TIMES {
	private int first, last, total;
	private boolean bFirst, bLast, bTotal;
	
	public SW_TIMES() {
		bFirst=bLast=bTotal=false;
		this.first=this.last=this.total=0;
	}
	
	public void onClear() {
		bFirst=bLast=bTotal=false;
		this.first=this.last=this.total=0;
	}

	public int getFirst() {
		return first;
	}

	public void setFirst(int first) {
		if(!bFirst)
			bFirst=true;
		this.first = first;
		if(bLast) {
			if(!bTotal)
				bTotal = true;
			this.total = this.last - this.first + 1;
		}
	}

	public int getLast() {
		return last;
	}

	public void setLast(int last) {
		if(!bLast)
			bLast=true;
		this.last = last;
		if(bFirst) {
			if(!bTotal)
				bTotal = true;
			this.total = this.last - this.first + 1;
		}
	}

	public int getTotal() {
		return total;
	}
	
	public boolean totalSet() {
		return bTotal;
	}
	public boolean firstSet() {
		return bFirst;
	}
	public boolean lastSet() {
		return bLast;
	}
}
