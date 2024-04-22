import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.project.adapters.OnItemClickListener
import com.example.project.databinding.ItemBillBinding
import com.example.project.models.Bill

class BillAdapter(private val bills: List<Bill>,private val listener: OnItemClickListener?) : RecyclerView.Adapter<BillAdapter.BillViewHolder>() {
    inner class BillViewHolder(private val binding: ItemBillBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener?.onItemClick(position)
                }
            }
        }
        fun bind(bill: Bill) {
            binding.billNumber.text = bill.number
            binding.billAmount.text = bill.amount.toString()
            binding.billDueDate.text = bill.due_date
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemBillBinding.inflate(inflater, parent, false)
        return BillViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        val bill = bills[position]
        holder.bind(bill)
    }

    override fun getItemCount(): Int {
        return bills.size
    }
}
